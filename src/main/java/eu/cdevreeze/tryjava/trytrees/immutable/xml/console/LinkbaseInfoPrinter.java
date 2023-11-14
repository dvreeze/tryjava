/*
 * Copyright 2023-2024 Chris de Vreeze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.cdevreeze.tryjava.trytrees.immutable.xml.console;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import eu.cdevreeze.tryjava.trytrees.immutable.xml.convert.SaxonConverter;
import eu.cdevreeze.tryjava.trytrees.immutable.xml.model.ElemNode;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import org.javimmutable.collections.ICollectors;
import org.javimmutable.collections.IList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;

/**
 * Program that prints some info about an XBRL linkbase (but not label or reference linkbase).
 * The linkbase file is referred to by a URI program parameter.
 *
 * @author Chris de Vreeze
 */
public class LinkbaseInfoPrinter {

    private static final Processor saxonProcessor = new Processor(false);

    private static final Logger logger = LoggerFactory.getLogger(LinkbaseInfoPrinter.class);

    public static void main(String[] args) throws SaxonApiException, IOException {
        Objects.requireNonNull(args);
        if (args.length != 1)
            throw new IllegalArgumentException(String.format("Expected precisely 1 argument (the input XML file path), but got %s ones", args.length));

        var inputFile = URI.create(args[0]);

        var docBuilder = saxonProcessor.newDocumentBuilder();

        logger.info("Parsing document ...");
        var docNode = docBuilder.build(new StreamSource(inputFile.toURL().openStream()));

        logger.atInfo().setMessage("Document parsed (as Saxon node): {}").addArgument(docNode.getBaseURI()).log();

        var xmlDocElem = (ElemNode) new SaxonConverter().convertToXmlNode(docNode);

        logger.info("Ready converting Saxon document to ElemNode");

        var linkbaseInfoPrinter = new LinkbaseInfoPrinter();
        var extendedLinks = linkbaseInfoPrinter.findAllExtendedLinks(xmlDocElem);
        var linkbase = new Linkbase(extendedLinks);

        logger.info("Ready creating 'Linkbase' from ElemNode");

        var objectMapper = new ObjectMapper();

        var module = new SimpleModule();
        module.addSerializer(Linkbase.class, new LinkbaseSerializer());
        objectMapper.registerModule(module);

        logger.atInfo()
                .setMessage("JSON output:\n{}")
                .addArgument(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(linkbase))
                .log();
    }

    public final String xlinkNs = "http://www.w3.org/1999/xlink";
    public final String linkNs = "http://www.xbrl.org/2003/linkbase";
    public final QName xlinkTypeName = new QName(xlinkNs, "type");
    public final QName xlinkHrefName = new QName(xlinkNs, "href");
    public final QName xlinkLabelName = new QName(xlinkNs, "label");
    public final QName xlinkArcroleName = new QName(xlinkNs, "arcrole");
    public final QName xlinkFromName = new QName(xlinkNs, "from");
    public final QName xlinkToName = new QName(xlinkNs, "to");
    public final QName xlinkRoleName = new QName(xlinkNs, "role");
    public final QName linkbaseName = new QName(linkNs, "linkbase");

    public enum XLinkType {
        SIMPLE,
        EXTENDED,
        LOC,
        ARC,
    }

    public sealed interface XLink permits ExtendedLink, Arc, Locator {
        XLinkType xlinkType();
    }

    public record Locator(URI xlinkHref, String xlinkLabel) implements XLink {
        public XLinkType xlinkType() {
            return XLinkType.LOC;
        }
    }

    public record Arc(String xlinkArcrole, String xlinkFrom, String xlinkTo, String order) implements XLink {
        public XLinkType xlinkType() {
            return XLinkType.ARC;
        }
    }

    public record ExtendedLink(String xlinkRole, IList<Arc> arcs, IList<Locator> locators) implements XLink {
        public XLinkType xlinkType() {
            return XLinkType.EXTENDED;
        }
    }

    public record Linkbase(IList<ExtendedLink> extendedLinks) {
    }

    private static class LinkbaseSerializer extends StdSerializer<Linkbase> {

        public LinkbaseSerializer() {
            this(null);
        }

        public LinkbaseSerializer(Class<Linkbase> t) {
            super(t);
        }

        @Override
        public void serialize(Linkbase value, JsonGenerator jgen, SerializerProvider provider) {
            try {
                jgen.writeStartObject();
                jgen.writeArrayFieldStart("extendedLinks");
                value.extendedLinks.stream().forEach(extLink -> serializeExtendedLink(extLink, jgen));
                jgen.writeEndArray();
                jgen.writeEndObject();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void serializeExtendedLink(ExtendedLink extendedLink, JsonGenerator jgen) {
            try {
                jgen.writeStartObject();
                jgen.writeStringField("xlinkRole", extendedLink.xlinkRole());
                jgen.writeArrayFieldStart("arcs");
                extendedLink.arcs.stream().forEach(arc -> serializeArc(arc, jgen));
                jgen.writeEndArray();
                jgen.writeArrayFieldStart("locators");
                extendedLink.locators.stream().forEach(locator -> serializeLocator(locator, jgen));
                jgen.writeEndArray();
                jgen.writeEndObject();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void serializeArc(Arc arc, JsonGenerator jgen) {
            try {
                jgen.writeStartObject();
                jgen.writeStringField("xlinkArcrole", arc.xlinkArcrole());
                jgen.writeStringField("xlinkFrom", arc.xlinkFrom());
                jgen.writeStringField("xlinkTo", arc.xlinkTo());
                jgen.writeStringField("order", arc.order());
                jgen.writeEndObject();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void serializeLocator(Locator locator, JsonGenerator jgen) {
            try {
                jgen.writeStartObject();
                jgen.writeStringField("xlinkHref", locator.xlinkHref().toString());
                jgen.writeStringField("xlinkLabel", locator.xlinkLabel());
                jgen.writeEndObject();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public IList<ExtendedLink> findAllExtendedLinks(ElemNode linkbaseElem) {
        if (!linkbaseName.equals(linkbaseElem.name())) {
            throw new IllegalArgumentException(String.format("Not a linkbase element name: '%s'", linkbaseElem.name()));
        }
        return linkbaseElem
                .findTopmostDescendants(e -> hasAttributeValue(e, xlinkTypeName, "extended"))
                .stream()
                .map(this::extractExtendedLink)
                .collect(ICollectors.toList());
    }

    private ExtendedLink extractExtendedLink(ElemNode extendedLinkElem) {
        assert hasAttributeValue(extendedLinkElem, xlinkTypeName, "extended");

        // Default role incorrect, is not empty
        return new ExtendedLink(
                extendedLinkElem.findAttributeValue(xlinkRoleName).orElse(""),
                findAllArcs(extendedLinkElem),
                findAllLocators(extendedLinkElem)
        );
    }

    private IList<Arc> findAllArcs(ElemNode extendedLinkElem) {
        assert hasAttributeValue(extendedLinkElem, xlinkTypeName, "extended");

        return extendedLinkElem
                .filterChildren(e -> hasAttributeValue(e, xlinkTypeName, "arc"))
                .stream()
                .map(e -> new Arc(e.getAttributeValue(xlinkArcroleName), e.getAttributeValue(xlinkFromName), e.getAttributeValue(xlinkToName), e.findAttributeValue(new QName("order")).orElse("1")))
                .collect(ICollectors.toList());
    }

    private IList<Locator> findAllLocators(ElemNode extendedLinkElem) {
        assert hasAttributeValue(extendedLinkElem, xlinkTypeName, "extended");

        return extendedLinkElem
                .filterChildren(e -> hasAttributeValue(e, xlinkTypeName, "locator"))
                .stream()
                .map(e -> new Locator(URI.create(e.getAttributeValue(xlinkHrefName)), e.getAttributeValue(xlinkLabelName)))
                .collect(ICollectors.toList());
    }

    private boolean hasAttribute(ElemNode elem, QName attrName) {
        return elem.findAttributeValue(attrName).isPresent();
    }

    private boolean hasAttributeValue(ElemNode elem, QName attrName, String attrValue) {
        return elem.findAttributeValue(attrName).stream().anyMatch(attrValue::equals);
    }
}
