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
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

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

        Serializer xmlSerializer = null;
        try {
            xmlSerializer = processor.newSerializer(System.out);
            xmlSerializer.setOutputProperty(Serializer.Property.INDENT, "yes");
            xmlSerializer.serializeNode(docXdmNode);
        } finally {
            if (xmlSerializer != null) xmlSerializer.close();
        }
    }

    // Recursion around IBody and IBodyElement.
    // Important: do not call method getBody on an IBodyElement, or else stack overflow will result!

    private static ElemNode printBody(IBody b) {
        return switch (b) {
            case XWPFDocument d -> printDocument(d);
            case XWPFTableCell c -> printTableCell(c);
            case XWPFHeader h -> printHeader(h);
            case XWPFFooter f -> printFooter(f);
            case XWPFFootnote n -> printFootnote(n);
            case XWPFEndnote n -> printEndnote(n);
            default -> printOtherBody(b);
        };
    }

    private static ElemNode printBodyElement(IBodyElement e) {
        return switch (e) {
            case XWPFParagraph p -> printParagraph(p);
            case XWPFTable t -> printTable(t);
            default -> printOtherBodyElement(e);
        };
    }

    // Printing IBodyElement instances (and their components)

    private static ElemNode printParagraph(XWPFParagraph p) {
        var childNodes =
                ImmutableList.<XmlNode>builder()
                        .add(makeTextElem("objectId", Objects.toIdentityString(p)))
                        .add(makeTextElem("elementType", p.getElementType().toString()))
                        .add(makeTextElem("partType", p.getPartType().toString()))
                        .add(makeTextElem("combinedText", p.getText()))
                        .addAll(p.getRuns().stream().map(r -> makeTextElem(
                                "text",
                                ImmutableMap.<String, String>builder()
                                        .putAll(
                                                ImmutableMap.of(
                                                        "isBold",
                                                        String.valueOf(r.isBold()),
                                                        "isItalic",
                                                        String.valueOf(r.isItalic())
                                                )
                                        )
                                        .putAll(
                                                ImmutableMap.of(
                                                                "color",
                                                                Optional.ofNullable(r.getColor()).orElse("")
                                                        )
                                                        .entrySet()
                                                        .stream()
                                                        .filter(kv -> !kv.getValue().isBlank())
                                                        .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue))
                                        )
                                        .build(),
                                r.text()
                        )).toList())
                        .build();
        return makeElem("paragraph", childNodes);
    }

    private static ElemNode printTable(XWPFTable t) {
        var childNodes =
                ImmutableList.<XmlNode>builder()
                        .add(makeTextElem("objectId", Objects.toIdentityString(t)))
                        .add(makeTextElem("elementType", t.getElementType().toString()))
                        .add(makeTextElem("partType", t.getPartType().toString()))
                        .addAll(
                                ImmutableList.<Optional<ElemNode>>builder()
                                        // bottom
                                        .add(
                                                Optional.ofNullable(t.getBottomBorderColor())
                                                        .map(v -> makeTextElem("bottomBorderColor", v))
                                        )
                                        .add(
                                                OptionalInt.of(t.getBottomBorderSize())
                                                        .stream()
                                                        .filter(v -> v != -1)
                                                        .mapToObj(v -> makeTextElem("bottomBorderSize", String.valueOf(v)))
                                                        .findFirst()
                                        )
                                        .add(
                                                OptionalInt.of(t.getBottomBorderSpace())
                                                        .stream()
                                                        .filter(v -> v != -1)
                                                        .mapToObj(v -> makeTextElem("bottomBorderSpace", String.valueOf(v)))
                                                        .findFirst()
                                        )
                                        .add(
                                                Optional.ofNullable(t.getBottomBorderType())
                                                        .map(v -> makeTextElem("bottomBorderType", v.toString()))
                                        )
                                        // left
                                        .add(
                                                Optional.ofNullable(t.getLeftBorderColor())
                                                        .map(v -> makeTextElem("leftBorderColor", v))
                                        )
                                        .add(
                                                OptionalInt.of(t.getLeftBorderSize())
                                                        .stream()
                                                        .filter(v -> v != -1)
                                                        .mapToObj(v -> makeTextElem("leftBorderSize", String.valueOf(v)))
                                                        .findFirst()
                                        )
                                        .add(
                                                OptionalInt.of(t.getLeftBorderSpace())
                                                        .stream()
                                                        .filter(v -> v != -1)
                                                        .mapToObj(v -> makeTextElem("leftBorderSpace", String.valueOf(v)))
                                                        .findFirst()
                                        )
                                        .add(
                                                Optional.ofNullable(t.getLeftBorderType())
                                                        .map(v -> makeTextElem("leftBorderType", v.toString()))
                                        )
                                        // right
                                        .add(
                                                Optional.ofNullable(t.getRightBorderColor())
                                                        .map(v -> makeTextElem("rightBorderColor", v))
                                        )
                                        .add(
                                                OptionalInt.of(t.getRightBorderSize())
                                                        .stream()
                                                        .filter(v -> v != -1)
                                                        .mapToObj(v -> makeTextElem("rightBorderSize", String.valueOf(v)))
                                                        .findFirst()
                                        )
                                        .add(
                                                OptionalInt.of(t.getRightBorderSpace())
                                                        .stream()
                                                        .filter(v -> v != -1)
                                                        .mapToObj(v -> makeTextElem("rightBorderSpace", String.valueOf(v)))
                                                        .findFirst()
                                        )
                                        .add(
                                                Optional.ofNullable(t.getRightBorderType())
                                                        .map(v -> makeTextElem("rightBorderType", v.toString()))
                                        )
                                        // top
                                        .add(
                                                Optional.ofNullable(t.getTopBorderColor())
                                                        .map(v -> makeTextElem("topBorderColor", v))
                                        )
                                        .add(
                                                OptionalInt.of(t.getTopBorderSize())
                                                        .stream()
                                                        .filter(v -> v != -1)
                                                        .mapToObj(v -> makeTextElem("topBorderSize", String.valueOf(v)))
                                                        .findFirst()
                                        )
                                        .add(
                                                OptionalInt.of(t.getTopBorderSpace())
                                                        .stream()
                                                        .filter(v -> v != -1)
                                                        .mapToObj(v -> makeTextElem("topBorderSpace", String.valueOf(v)))
                                                        .findFirst()
                                        )
                                        .add(
                                                Optional.ofNullable(t.getTopBorderType())
                                                        .map(v -> makeTextElem("topBorderType", v.toString()))
                                        )
                                        // inside horizontal border
                                        .add(
                                                Optional.ofNullable(t.getInsideHBorderColor())
                                                        .map(v -> makeTextElem("insideHBorderColor", v))
                                        )
                                        .add(
                                                OptionalInt.of(t.getInsideHBorderSize())
                                                        .stream()
                                                        .filter(v -> v != -1)
                                                        .mapToObj(v -> makeTextElem("insideHBorderSize", String.valueOf(v)))
                                                        .findFirst()
                                        )
                                        .add(
                                                OptionalInt.of(t.getInsideHBorderSpace())
                                                        .stream()
                                                        .filter(v -> v != -1)
                                                        .mapToObj(v -> makeTextElem("insideHBorderSpace", String.valueOf(v)))
                                                        .findFirst()
                                        )
                                        .add(
                                                Optional.ofNullable(t.getInsideHBorderType())
                                                        .map(v -> makeTextElem("insideHBorderType", v.toString()))
                                        )
                                        // inside vertical border
                                        .add(
                                                Optional.ofNullable(t.getInsideVBorderColor())
                                                        .map(v -> makeTextElem("insideVBorderColor", v))
                                        )
                                        .add(
                                                OptionalInt.of(t.getInsideVBorderSize())
                                                        .stream()
                                                        .filter(v -> v != -1)
                                                        .mapToObj(v -> makeTextElem("insideVBorderSize", String.valueOf(v)))
                                                        .findFirst()
                                        )
                                        .add(
                                                OptionalInt.of(t.getInsideVBorderSpace())
                                                        .stream()
                                                        .filter(v -> v != -1)
                                                        .mapToObj(v -> makeTextElem("insideVBorderSpace", String.valueOf(v)))
                                                        .findFirst()
                                        )
                                        .add(
                                                Optional.ofNullable(t.getInsideVBorderType())
                                                        .map(v -> makeTextElem("insideVBorderType", v.toString()))
                                        )
                                        // other properties
                                        .add(
                                                OptionalInt.of(t.getCellMarginBottom())
                                                        .stream()
                                                        .filter(v -> v != -1)
                                                        .mapToObj(v -> makeTextElem("cellMarginBottom", String.valueOf(v)))
                                                        .findFirst()
                                        )
                                        .add(
                                                OptionalInt.of(t.getCellMarginLeft())
                                                        .stream()
                                                        .filter(v -> v != -1)
                                                        .mapToObj(v -> makeTextElem("cellMarginLeft", String.valueOf(v)))
                                                        .findFirst()
                                        )
                                        .add(
                                                OptionalInt.of(t.getCellMarginRight())
                                                        .stream()
                                                        .filter(v -> v != -1)
                                                        .mapToObj(v -> makeTextElem("cellMarginRight", String.valueOf(v)))
                                                        .findFirst()
                                        )
                                        .add(
                                                OptionalInt.of(t.getCellMarginTop())
                                                        .stream()
                                                        .filter(v -> v != -1)
                                                        .mapToObj(v -> makeTextElem("cellMarginTop", String.valueOf(v)))
                                                        .findFirst()
                                        )
                                        .add(
                                                Optional.ofNullable(t.getTableAlignment())
                                                        .map(v -> makeTextElem("tableAlignment", v.toString()))
                                        )
                                        .add(
                                                OptionalDouble.of(t.getWidthDecimal())
                                                        .stream()
                                                        .mapToObj(v -> makeTextElem("width", String.valueOf(v)))
                                                        .findFirst()
                                        )
                                        .add(
                                                Optional.ofNullable(t.getWidthType())
                                                        .map(v -> makeTextElem("widthType", v.toString()))
                                        )
                                        .build()
                                        .stream()
                                        .flatMap(Optional::stream)
                                        .collect(ImmutableList.toImmutableList())
                        )
                        .addAll(t.getRows().stream().map(ResumeContentPrinter::printTableRow).toList())
                        .build();
        return makeElem("table", childNodes);
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

    private static ElemNode printTableRow(XWPFTableRow r) {
        var childNodes =
                ImmutableList.<XmlNode>builder()
                        .add(makeTextElem("objectId", Objects.toIdentityString(r)))
                        .addAll(
                                ImmutableList.<Optional<ElemNode>>builder()
                                        .add(
                                                OptionalInt.of(r.getHeight())
                                                        .stream()
                                                        .filter(v -> v != -1)
                                                        .mapToObj(v -> makeTextElem("height", String.valueOf(v)))
                                                        .findFirst()
                                        )
                                        .add(
                                                Optional.ofNullable(r.getHeightRule())
                                                        .map(v -> makeTextElem("heightRule", v.toString()))
                                        )
                                        .build()
                                        .stream()
                                        .flatMap(Optional::stream)
                                        .collect(ImmutableList.toImmutableList())
                        )
                        .addAll(r.getTableCells().stream().map(ResumeContentPrinter::printTableCell).toList())
                        .build();
        return makeElem("tableRow", childNodes);
    }

    // Printing IBody instances

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
                                makeElem(
                                        "headers",
                                        d.getHeaderList().stream().map(ResumeContentPrinter::printHeader).collect(ImmutableList.toImmutableList())
                                )
                        )
                        .addAll(d.getBodyElements().stream().map(ResumeContentPrinter::printBodyElement).toList())
                        .add(
                                makeElem(
                                        "footers",
                                        d.getFooterList().stream().map(ResumeContentPrinter::printFooter).collect(ImmutableList.toImmutableList())
                                )
                        )
                        .add(
                                makeElem(
                                        "footnotes",
                                        d.getFootnotes().stream().map(ResumeContentPrinter::printFootnote).collect(ImmutableList.toImmutableList())
                                )
                        )
                        .add(
                                makeElem(
                                        "endnotes",
                                        d.getEndnotes().stream().map(ResumeContentPrinter::printEndnote).collect(ImmutableList.toImmutableList())
                                )
                        )
                        .build();
        return makeElem("document", childNodes);
    }

    private static ElemNode printTableCell(XWPFTableCell c) {
        var childNodes =
                ImmutableList.<XmlNode>builder()
                        .add(makeTextElem("objectId", Objects.toIdentityString(c)))
                        .add(makeTextElem("partType", c.getPartType().toString()))
                        .addAll(
                                ImmutableList.<Optional<ElemNode>>builder()
                                        .add(
                                                Optional.ofNullable(c.getColor())
                                                        .map(v -> makeTextElem("color", v))
                                        )
                                        .add(
                                                OptionalDouble.of(c.getWidthDecimal())
                                                        .stream()
                                                        .mapToObj(v -> makeTextElem("width", String.valueOf(v)))
                                                        .findFirst()
                                        )
                                        .add(
                                                Optional.ofNullable(c.getWidthType())
                                                        .map(v -> makeTextElem("widthType", v.toString()))
                                        )
                                        .build()
                                        .stream()
                                        .flatMap(Optional::stream)
                                        .collect(ImmutableList.toImmutableList())
                        )
                        .addAll(c.getBodyElements().stream().map(ResumeContentPrinter::printBodyElement).toList())
                        .build();
        return makeElem("tableCell", childNodes);
    }

    private static ElemNode printHeader(XWPFHeader h) {
        var childNodes =
                ImmutableList.<XmlNode>builder()
                        .add(makeTextElem("objectId", Objects.toIdentityString(h)))
                        .add(makeTextElem("partType", h.getPartType().toString()))
                        .add(makeTextElem("text", h.getText()))
                        .addAll(h.getBodyElements().stream().map(ResumeContentPrinter::printBodyElement).toList())
                        .build();
        return makeElem("header", childNodes);
    }

    private static ElemNode printFooter(XWPFFooter f) {
        var childNodes =
                ImmutableList.<XmlNode>builder()
                        .add(makeTextElem("objectId", Objects.toIdentityString(f)))
                        .add(makeTextElem("partType", f.getPartType().toString()))
                        .add(makeTextElem("text", f.getText()))
                        .addAll(f.getBodyElements().stream().map(ResumeContentPrinter::printBodyElement).toList())
                        .build();
        return makeElem("footer", childNodes);
    }

    private static ElemNode printFootnote(XWPFFootnote n) {
        var childNodes =
                ImmutableList.<XmlNode>builder()
                        .add(makeTextElem("objectId", Objects.toIdentityString(n)))
                        .add(makeTextElem("partType", n.getPartType().toString()))
                        .addAll(n.getBodyElements().stream().map(ResumeContentPrinter::printBodyElement).toList())
                        .build();
        return makeElem("footnote", childNodes);
    }

    private static ElemNode printEndnote(XWPFEndnote n) {
        var childNodes =
                ImmutableList.<XmlNode>builder()
                        .add(makeTextElem("objectId", Objects.toIdentityString(n)))
                        .add(makeTextElem("partType", n.getPartType().toString()))
                        .addAll(n.getBodyElements().stream().map(ResumeContentPrinter::printBodyElement).toList())
                        .build();
        return makeElem("endnote", childNodes);
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

        static ElemNode makeElem(String name, ImmutableMap<String, String> attrs, ImmutableList<XmlNode> children) {
            return new ElemNode(
                    new QName(name),
                    attrs.entrySet().stream().collect(ImmutableMap
                            .toImmutableMap((Map.Entry<String, String> kv) -> new QName(kv.getKey()), Map.Entry::getValue)),
                    children
            );
        }

        static ElemNode makeTextElem(String name, ImmutableMap<String, String> attrs, String text) {
            return makeElem(name, attrs, ImmutableList.of(new TextNode(text)));
        }

        static ElemNode makeElem(String name, ImmutableList<XmlNode> children) {
            return makeElem(name, ImmutableMap.of(), children);
        }

        static ElemNode makeTextElem(String name, String text) {
            return makeTextElem(name, ImmutableMap.of(), text);
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
