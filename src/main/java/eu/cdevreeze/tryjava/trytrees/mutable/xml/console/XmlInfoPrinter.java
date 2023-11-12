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
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNodeKind;

import javax.xml.namespace.QName;
import java.io.File;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Program that prints some info about an input XML document
 *
 * @author Chris de Vreeze
 */
public class XmlInfoPrinter {

    public record XmlDocInfo(Path docPath, int nrOfElems, Map<QName, Long> elemCounts) {

        public static XmlDocInfo fromDoc(Path docPath, ElemNode docElem) {
            var allElems = docElem.findAllDescendantsOrSelf();

            var elemCounts = allElems.stream().collect(Collectors.groupingBy(ElemNode::name, Collectors.counting()));

            return new XmlDocInfo(docPath, allElems.size(), elemCounts);
        }
    }

    private static final Processor saxonProcessor = new Processor(false);

    public static void main(String[] args) throws SaxonApiException {
        Objects.requireNonNull(args);
        if (args.length != 1)
            throw new IllegalArgumentException(String.format("Expected precisely 1 argument (the input XML file path), but got %s ones", args.length));

        var inputFile = new File(args[0]);

        var docBuilder = saxonProcessor.newDocumentBuilder();
        var docNode = docBuilder.build(inputFile);

        System.out.printf("Document parsed (as Saxon node): '%s'%n", docNode.getBaseURI());
        System.out.printf(
                "Number of (Saxon) elements: %s%n",
                docNode.axisIterator(Axis.DESCENDANT_OR_SELF).stream().filter(n -> n.getNodeKind().equals(XdmNodeKind.ELEMENT)).asListOfNodes().size());

        var xmlDocElem = (ElemNode) new SaxonConverter().convertToXmlNode(docNode);

        printXmlFileInfo(inputFile.toPath(), xmlDocElem);
    }

    public static void printXmlFileInfo(Path docPath, ElemNode xmlDocElem) {
        var xmlDocInfo = XmlDocInfo.fromDoc(docPath, xmlDocElem);

        System.out.println();
        System.out.printf("Document path: '%s'%n", xmlDocInfo.docPath);
        System.out.printf("Number of element nodes:  %s%n", xmlDocInfo.nrOfElems);

        System.out.println();
        xmlDocInfo.elemCounts
                .entrySet()
                .stream()
                .toList()
                .stream()
                .sorted(Comparator.comparingLong((Map.Entry<QName, Long> kv) -> kv.getValue()).reversed())
                .forEach(kv -> System.out.printf("Element '%s' occurs %s times%n", kv.getKey(), kv.getValue()));
    }
}
