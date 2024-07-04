/*
 * Copyright 2024-2024 Chris de Vreeze
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

package eu.cdevreeze.tryjava.tryel.console;

import eu.cdevreeze.tryjava.tryxml.convert.SaxonConverter;
import eu.cdevreeze.tryjava.tryxml.parentaware.DocumentElement;
import eu.cdevreeze.tryjava.tryxml.simple.Element;
import jakarta.el.ELProcessor;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Program that prints some info about an XBRL linkbase (but not label or reference linkbase), using Jakarta EL.
 * The linkbase file is referred to by a URI program parameter.
 *
 * @author Chris de Vreeze
 */
public class LinkbaseInfoPrinterUsingEl {

    public record Arc(String extendedLinkrole, String arcrole, String order, URI from, URI to) {
    }

    private static final Processor saxonProcessor = new Processor(false);

    private static final Logger logger = LoggerFactory.getLogger(LinkbaseInfoPrinterUsingEl.class);

    private static final String XLINK_NS = "http://www.w3.org/1999/xlink";

    public static void main(String[] args) throws IOException, SaxonApiException, NoSuchMethodException {
        Objects.requireNonNull(args);
        if (args.length != 1)
            throw new IllegalArgumentException(String.format("Expected precisely 1 argument (the input XML file path), but got %s ones", args.length));

        var inputFile = URI.create(args[0]);

        DocumentElement.Element docElem = parseDocumentElement(inputFile);

        ELProcessor elProcessor = new ELProcessor();
        elProcessor.defineBean("documentElement", docElem);

        QName docElemName = elProcessor.eval("documentElement.elementName()");

        System.out.printf("Document element name: %s%n", docElemName);

        elProcessor.defineFunction(
                "cv",
                "attribute",
                LinkbaseInfoPrinterUsingEl.class.getDeclaredMethod("attribute", DocumentElement.Element.class, String.class, String.class));

        List<String> linkRoles = elProcessor.eval("""
                documentElement.childElementStream().toList().stream()
                    .filter(e -> e.elementName().localPart == "roleRef")
                    .map(e -> cv:attribute(e, "", "roleURI"))
                    .toList()
                """.strip()
        );

        for (String linkRole : linkRoles) {
            System.out.printf("Referenced link role: %s%n", linkRole);
        }

        elProcessor.defineFunction(
                "cv",
                "isExtendedLink",
                LinkbaseInfoPrinterUsingEl.class.getDeclaredMethod("isExtendedLink", DocumentElement.Element.class));

        elProcessor.defineFunction(
                "cv",
                "findAllArcs",
                LinkbaseInfoPrinterUsingEl.class.getDeclaredMethod("findAllArcs", DocumentElement.Element.class));

        List<DocumentElement.Element> extendedLinks = elProcessor.eval("""
                documentElement.childElementStream().toList().stream()
                    .filter(e -> cv:isExtendedLink(e))
                    .toList()
                """.strip()
        );

        List<Arc> arcs = elProcessor.eval("""
                documentElement.childElementStream().toList().stream()
                    .filter(e -> cv:isExtendedLink(e))
                    .flatMap(e -> cv:findAllArcs(e).stream())
                    .toList()
                """.strip());

        for (var extendedLink : extendedLinks) {
            System.out.println();
            String elr = attribute(extendedLink, XLINK_NS, "role");

            System.out.printf(
                    "Extended link: %s. Link role: %s%n",
                    extendedLink.elementName(),
                    elr);
            System.out.println();

            for (var arc : arcs.stream().filter(arc -> arc.extendedLinkrole.equals(elr)).toList()) {
                System.out.printf("Arc: %s%n", arc);
            }
        }
    }

    public static boolean isExtendedLink(DocumentElement.Element element) {
        return Set.of("calculationLink", "presentationLink", "definitionLink").contains(element.elementName().getLocalPart());
    }

    public static String attribute(DocumentElement.Element element, String namespace, String attributeName) {
        return element.attributes().get(new QName(namespace, attributeName));
    }

    public static List<Arc> findAllArcs(DocumentElement.Element ancestor) {
        List<String> linkroles = ancestor.elementStream(LinkbaseInfoPrinterUsingEl::isExtendedLink)
                .map(e -> attribute(e, XLINK_NS, "role")).distinct().toList();

        return linkroles.stream().flatMap(role -> findAllArcs(ancestor, role)).toList();
    }

    private static Stream<Arc> findAllArcs(DocumentElement.Element ancestor, String extendedLinkrole) {
        DocumentElement.Element extendedLink =
                ancestor.elementStream(e ->
                                isExtendedLink(e) &&
                                        attribute(e, XLINK_NS, "role").equals(extendedLinkrole))
                        .findFirst().orElseThrow();

        Map<String, List<DocumentElement.Element>> locatorsByXlinkLabel =
                extendedLink
                        .childElementStream(
                                e -> e.elementName().equals(new QName("http://www.xbrl.org/2003/linkbase", "loc"))
                        )
                        .collect(Collectors.groupingBy(e -> attribute(e, XLINK_NS, "label")));

        return extendedLink
                .childElementStream(e -> attribute(e, XLINK_NS, "type").equals("arc"))
                .flatMap(arc -> {
                    List<DocumentElement.Element> fromLocators =
                            locatorsByXlinkLabel.get(attribute(arc, XLINK_NS, "from"));
                    List<DocumentElement.Element> toLocators =
                            locatorsByXlinkLabel.get(attribute(arc, XLINK_NS, "to"));

                    return fromLocators.stream()
                            .flatMap(fromLoc -> toLocators.stream().map(toLoc -> new Arc(
                                    extendedLinkrole,
                                    attribute(arc, XLINK_NS, "arcrole"),
                                    attribute(arc, "", "order"),
                                    URI.create(attribute(fromLoc, XLINK_NS, "href")),
                                    URI.create(attribute(toLoc, XLINK_NS, "href"))
                            )));
                });
    }

    private static DocumentElement.Element parseDocumentElement(URI inputFile) throws IOException, SaxonApiException {
        var docBuilder = saxonProcessor.newDocumentBuilder();

        logger.info("Parsing document ...");
        var docNode = docBuilder.build(new StreamSource(inputFile.toURL().openStream()));

        logger.atInfo().setMessage("Document parsed (as Saxon node): {}").addArgument(docNode.getBaseURI()).log();

        Element simpleDocElem = (Element) new SaxonConverter().convertToXmlNode(docNode);

        logger.info("Ready converting Saxon document to simple.Element");

        DocumentElement.Element docElem = DocumentElement.create(simpleDocElem).documentElement();

        logger.info("Ready converting simple.Element to DocumentElement.Element");

        return docElem;
    }
}
