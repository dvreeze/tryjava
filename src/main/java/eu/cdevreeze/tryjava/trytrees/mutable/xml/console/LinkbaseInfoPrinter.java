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

package eu.cdevreeze.tryjava.trytrees.mutable.xml.console;

import eu.cdevreeze.tryjava.trytrees.mutable.xml.convert.SaxonConverter;
import eu.cdevreeze.tryjava.trytrees.mutable.xml.model.ElemNode;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;

import javax.xml.namespace.QName;
import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Objects;

/**
 * Program that prints some info about an XBRL linkbase (but not label or reference linkbase)
 *
 * @author Chris de Vreeze
 */
public class LinkbaseInfoPrinter {

    private static final Processor saxonProcessor = new Processor(false);

    public static void main(String[] args) throws SaxonApiException {
        Objects.requireNonNull(args);
        if (args.length != 1)
            throw new IllegalArgumentException(String.format("Expected precisely 1 argument (the input XML file path), but got %s ones", args.length));

        var inputFile = new File(args[0]);

        var docBuilder = saxonProcessor.newDocumentBuilder();
        var docNode = docBuilder.build(inputFile);

        var xmlDocElem = (ElemNode) new SaxonConverter().convertToXmlNode(docNode);

        var linkbaseInfoPrinter = new LinkbaseInfoPrinter();
        var extendedLinks = linkbaseInfoPrinter.findAllExtendedLinks(xmlDocElem);

        extendedLinks.stream().forEach(extLink -> {
            System.out.println();
            System.out.println("Extended link");
            System.out.printf("Extended link role: %s%n", extLink.xlinkRole);

            System.out.println();
            System.out.println("Extended link arcs");
            extLink.arcs.stream().forEach(arc -> System.out.println(arc));

            System.out.println();
            System.out.println("Extended link locators");
            extLink.locators.stream().forEach(loc -> System.out.println(loc));
        });
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

    enum XLinkType {
        SIMPLE,
        EXTENDED,
        LOC,
        ARC,
    }

    public record Locator(URI xlinkHref, String xlinkLabel) {
        public XLinkType xlinkType() {
            return XLinkType.LOC;
        }
    }

    public record Arc(String xlinkArcrole, String xlinkFrom, String xlinkTo, String order) {
        public XLinkType xlinkType() {
            return XLinkType.ARC;
        }
    }

    public record ExtendedLink(String xlinkRole, List<Arc> arcs, List<Locator> locators) {
        public XLinkType xlinkType() {
            return XLinkType.EXTENDED;
        }
    }

    public List<ExtendedLink> findAllExtendedLinks(ElemNode linkbaseElem) {
        if (!linkbaseName.equals(linkbaseElem.name())) {
            throw new IllegalArgumentException(String.format("Not a linkbase element name: '%s'", linkbaseElem.name()));
        }
        return linkbaseElem
                .filterDescendants(e -> hasAttributeValue(e, xlinkTypeName, "extended"))
                .stream()
                .map(e -> extractExtendedLink(e))
                .toList();
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

    private List<Arc> findAllArcs(ElemNode extendedLinkElem) {
        assert hasAttributeValue(extendedLinkElem, xlinkTypeName, "extended");

        return extendedLinkElem
                .filterChildren(e -> hasAttributeValue(e, xlinkTypeName, "arc"))
                .stream()
                .map(e -> new Arc(e.getAttributeValue(xlinkArcroleName), e.getAttributeValue(xlinkFromName), e.getAttributeValue(xlinkToName), e.findAttributeValue(new QName("order")).orElse("1")))
                .toList();
    }

    private List<Locator> findAllLocators(ElemNode extendedLinkElem) {
        assert hasAttributeValue(extendedLinkElem, xlinkTypeName, "extended");

        return extendedLinkElem
                .filterChildren(e -> hasAttributeValue(e, xlinkTypeName, "locator"))
                .stream()
                .map(e -> new Locator(URI.create(e.getAttributeValue(xlinkHrefName)), e.getAttributeValue(xlinkLabelName)))
                .toList();
    }

    private boolean hasAttribute(ElemNode elem, QName attrName) {
        return elem.findAttributeValue(attrName).isPresent();
    }

    private boolean hasAttributeValue(ElemNode elem, QName attrName, String attrValue) {
        return elem.findAttributeValue(attrName).stream().anyMatch(v -> attrValue.equals(v));
    }
}
