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

package eu.cdevreeze.tryjava.trypoi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import eu.cdevreeze.tryjava.trytrees.guavaimmutable.xml.model.ElemNode;
import eu.cdevreeze.tryjava.trytrees.guavaimmutable.xml.model.TextNode;
import eu.cdevreeze.tryjava.trytrees.guavaimmutable.xml.model.XmlNode;
import net.sf.saxon.s9api.*;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static eu.cdevreeze.tryjava.trypoi.ResumeContentPrinter.XmlSupport.makeElem;
import static eu.cdevreeze.tryjava.trypoi.ResumeContentPrinter.XmlSupport.makeTextElem;

/**
 * Program that extracts and prints the content of a The.NextGen resume as XML. The program
 * takes one program argument, namely the path of the input document.
 * <p>
 * The implementation depends on the "trytrees.guavaimmutable" namespace for the XML support.
 * <p>
 * Note that a Microsoft docx file is itself a zipped file containing XML files.
 * The XML printed by this program is a lot simpler, though.
 * <p>
 * Also note that XML is a better format than JSON for holding potentially long text strings.
 *
 * @author Chris de Vreeze
 */
public final class ResumeContentPrinter {

    public static void main(String[] args) throws IOException, SaxonApiException {
        // See https://poi.apache.org/components/logging.html
        System.setProperty("log4j2.loggerContextFactory", "org.apache.logging.log4j.simple.SimpleLoggerContextFactory");

        Objects.checkIndex(0, args.length);
        var docPath = Paths.get(args[0]);
        var doc = new XWPFDocument(Files.newInputStream(docPath));

        var extractor = new XWPFWordExtractor(doc);

        var corePropsElem = makeElem(
                "coreProperties",
                ImmutableList.of(
                                Optional.ofNullable(extractor.getCoreProperties().getCreator()).map(v -> makeTextElem("creator", v)),
                                Optional.ofNullable(extractor.getCoreProperties().getTitle()).map(v -> makeTextElem("title", v)),
                                Optional.ofNullable(extractor.getCoreProperties().getDescription()).map(v -> makeTextElem("description", v)),
                                Optional.ofNullable(extractor.getCoreProperties().getContentType()).map(v -> makeTextElem("contentType", v)),
                                Optional.ofNullable(extractor.getCoreProperties().getContentStatus()).map(v -> makeTextElem("contentStatus", v)),
                                Optional.ofNullable(extractor.getCoreProperties().getCreated()).map(v -> makeTextElem("created", v.toString())),
                                Optional.ofNullable(extractor.getCoreProperties().getModified()).map(v -> makeTextElem("modified", v.toString())),
                                Optional.ofNullable(extractor.getCoreProperties().getLastModifiedByUser()).map(v -> makeTextElem("lastModifiedByUser", v))
                        ).stream()
                        .flatMap(Optional::stream)
                        .collect(ImmutableList.toImmutableList())
        );

        var resultElem = makeElem(
                "documentWithProperties",
                ImmutableList.of(corePropsElem, printBody(doc))
        );

        var processor = new Processor(false);
        var converterToSaxon = ConverterToSaxon.newInstance(processor);

        converterToSaxon.writeDocument(resultElem);
        var docXdmNode = converterToSaxon.getDocumentNode();

        var xmlSerializer = processor.newSerializer(System.out);
        xmlSerializer.setOutputProperty(Serializer.Property.INDENT, "yes");
        xmlSerializer.serializeNode(docXdmNode);
    }

    // Recursion around IBody and IBodyElement.
    // Important: do not call method getBody on an IBodyElement, or else stack overflow will result!

    private static ElemNode printBody(IBody b) {
        return switch (b) {
            case XWPFDocument d -> printDocument(d);
            case XWPFTableCell c -> printTableCell(c);
            default -> printOtherBody(b);
        };
    }

    private static ElemNode printBodyElement(IBodyElement e) {
        return switch (e) {
            case XWPFParagraph p -> printParagraph(p);
            case XWPFTable t -> printTable(t);
            case XWPFTableCell c -> printTableCell(c);
            default -> printOtherBodyElement(e);
        };
    }

    private static ElemNode printParagraph(XWPFParagraph p) {
        var childNodes =
                ImmutableList.<XmlNode>builder()
                        .add(makeTextElem("objectId", Objects.toIdentityString(p)))
                        .add(makeTextElem("elementType", p.getElementType().toString()))
                        .add(makeTextElem("partType", p.getPartType().toString()))
                        .addAll(p.getRuns().stream().map(r -> makeTextElem("text", r.text())).toList())
                        .build();
        return makeElem("paragraph", childNodes);
    }

    private static ElemNode printTable(XWPFTable t) {
        var childNodes =
                ImmutableList.<XmlNode>builder()
                        .add(makeTextElem("objectId", Objects.toIdentityString(t)))
                        .add(makeTextElem("elementType", t.getElementType().toString()))
                        .add(makeTextElem("partType", t.getPartType().toString()))
                        .addAll(t.getRows().stream().map(ResumeContentPrinter::printTableRow).toList())
                        .build();
        return makeElem("table", childNodes);
    }

    private static ElemNode printTableRow(XWPFTableRow r) {
        var childNodes =
                ImmutableList.<XmlNode>builder()
                        .add(makeTextElem("objectId", Objects.toIdentityString(r)))
                        .addAll(r.getTableCells().stream().map(ResumeContentPrinter::printTableCell).toList())
                        .build();
        return makeElem("tableRow", childNodes);
    }

    private static ElemNode printDocument(XWPFDocument d) {
        var childNodes =
                ImmutableList.<XmlNode>builder()
                        .add(makeTextElem("objectId", Objects.toIdentityString(d)))
                        .add(makeTextElem("partType", d.getPartType().toString()))
                        .addAll(
                                Optional.ofNullable(d.getProperties().getCoreProperties().getIdentifier())
                                        .map(id -> makeTextElem("identifier", id))
                                        .stream()
                                        .toList()
                        )
                        .add(
                                makeTextElem(
                                        "headers",
                                        d.getHeaderList().stream().map(POIXMLDocumentPart::toString).collect(Collectors.joining(", "))
                                )
                        )
                        .addAll(d.getBodyElements().stream().map(ResumeContentPrinter::printBodyElement).toList())
                        .build();
        return makeElem("document", childNodes);
    }

    private static ElemNode printTableCell(XWPFTableCell c) {
        var childNodes =
                ImmutableList.<XmlNode>builder()
                        .add(makeTextElem("objectId", Objects.toIdentityString(c)))
                        .addAll(c.getBodyElements().stream().map(ResumeContentPrinter::printBodyElement).toList())
                        .build();
        return makeElem("tableCell", childNodes);
    }

    private static ElemNode printOtherBodyElement(IBodyElement e) {
        var childNodes =
                ImmutableList.<XmlNode>builder()
                        .add(makeTextElem("objectId", Objects.toIdentityString(e)))
                        .add(makeTextElem("elementType", e.getElementType().toString()))
                        .add(makeTextElem("partType", e.getPartType().toString()))
                        .build();
        return makeElem("otherBodyElement", childNodes);
    }

    private static ElemNode printOtherBody(IBody b) {
        var childNodes =
                ImmutableList.<XmlNode>builder()
                        .add(makeTextElem("objectId", Objects.toIdentityString(b)))
                        .add(makeTextElem("partType", b.getPartType().toString()))
                        .addAll(b.getBodyElements().stream().map(ResumeContentPrinter::printBodyElement).toList())
                        .build();
        return makeElem("otherBody", childNodes);
    }

    static final class XmlSupport {

        static ElemNode makeElem(String name, ImmutableList<XmlNode> children) {
            return new ElemNode(new QName(name), ImmutableMap.of(), children);
        }

        static ElemNode makeTextElem(String name, String text) {
            return makeElem(name, ImmutableList.of(new TextNode(text)));
        }
    }

    static final class ConverterToSaxon {

        private final BuildingStreamWriter xmlStreamWriter;

        ConverterToSaxon(BuildingStreamWriter xmlStreamWriter) {
            this.xmlStreamWriter = xmlStreamWriter;
        }

        static ConverterToSaxon newInstance(Processor processor) {
            try {
                return new ConverterToSaxon(processor.newDocumentBuilder().newBuildingStreamWriter());
            } catch (SaxonApiException e) {
                throw new RuntimeException(e);
            }
        }

        XdmNode getDocumentNode() {
            try {
                return xmlStreamWriter.getDocumentNode();
            } catch (SaxonApiException e) {
                throw new RuntimeException(e);
            }
        }

        void writeDocument(ElemNode elem) {
            try {
                xmlStreamWriter.writeStartDocument("1.0");
                writeElement(elem);
                xmlStreamWriter.writeEndDocument();
                ;
            } catch (XMLStreamException e) {
                throw new RuntimeException(e);
            }
        }

        void writeElement(ElemNode elem) {
            try {
                xmlStreamWriter.writeStartElement(elem.name().getPrefix(), elem.name().getLocalPart(), elem.name().getNamespaceURI());

                elem.attributes().forEach((attrName, attrValue) -> {
                    try {
                        xmlStreamWriter.writeAttribute(
                                attrName.getPrefix(),
                                attrName.getNamespaceURI(),
                                attrName.getLocalPart(),
                                attrValue
                        );
                    } catch (XMLStreamException e) {
                        throw new RuntimeException(e);
                    }
                });

                elem.childNodes().forEach(ch -> {
                    switch (ch) {
                        case TextNode t -> writeText(t);
                        case ElemNode e -> writeElement(e);
                    }
                });

                xmlStreamWriter.writeEndElement();
            } catch (XMLStreamException e) {
                throw new RuntimeException(e);
            }
        }

        void writeText(TextNode text) {
            try {
                xmlStreamWriter.writeCharacters(text.text());
            } catch (XMLStreamException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
