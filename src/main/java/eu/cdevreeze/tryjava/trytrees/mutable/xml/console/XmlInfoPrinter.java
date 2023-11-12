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

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.cdevreeze.tryjava.trytrees.mutable.xml.convert.SaxonConverter;
import eu.cdevreeze.tryjava.trytrees.mutable.xml.model.ElemNode;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNodeKind;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Program that prints some info about an input XML document
 *
 * @author Chris de Vreeze
 */
public class XmlInfoPrinter {

    public record ElementCount(QName elementName, long elementCount) {
    }

    public record XmlDocInfo(Path docPath, int nrOfElems, List<ElementCount> elemCounts) {

        public static XmlDocInfo fromDoc(Path docPath, ElemNode docElem) {
            var allElems = docElem.findAllDescendantsOrSelf();

            var elemCounts = allElems.stream().collect(Collectors.groupingBy(
                            ElemNode::name,
                            Collectors.counting()))
                    .entrySet()
                    .stream()
                    .map(kv -> new ElementCount(kv.getKey(), kv.getValue()))
                    .sorted(
                            Comparator.comparingLong(ElementCount::elementCount).reversed()
                                    .thenComparing(elemCnt -> elemCnt.elementName().toString()))
                    .toList();

            return new XmlDocInfo(docPath, allElems.size(), elemCounts);
        }
    }

    private static final Processor saxonProcessor = new Processor(false);

    public static void main(String[] args) throws SaxonApiException, IOException {
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

        System.out.println();
        System.out.println("Ready converting Saxon document to ElemNode");

        var xmlDocInfo = XmlDocInfo.fromDoc(inputFile.toPath(), xmlDocElem);

        System.out.println();
        System.out.println("Ready creating 'XmlDocInfo' from ElemNode");

        var objectMapper = new ObjectMapper();

        System.out.println();
        System.out.println("JSON output:");
        System.out.println();

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(System.out, xmlDocInfo);
    }
}
