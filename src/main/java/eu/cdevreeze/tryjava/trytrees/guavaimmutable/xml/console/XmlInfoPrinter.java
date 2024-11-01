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

package eu.cdevreeze.tryjava.trytrees.guavaimmutable.xml.console;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.ImmutableList;
import eu.cdevreeze.tryjava.trytrees.guavaimmutable.xml.convert.SaxonConverter;
import eu.cdevreeze.tryjava.trytrees.guavaimmutable.xml.model.ElemNode;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNodeKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.net.URI;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Program that prints some info about an input XML document (referred to by a URI program parameter)
 *
 * @author Chris de Vreeze
 */
public class XmlInfoPrinter {

    public record ElementCount(QName elementName, long elementCount) {
    }

    public record XmlDocInfo(URI docUri, int nrOfElems, ImmutableList<ElementCount> elemCounts) {

        public static XmlDocInfo fromDoc(URI docUri, ElemNode docElem) {
            var allElems = docElem.findAllDescendantsOrSelf();

            var elemsGroupedByName = allElems.stream().collect(Collectors.groupingBy(ElemNode::name));

            var elemCounts = ImmutableList.copyOf(elemsGroupedByName.entrySet())
                    .stream()
                    .map(entry -> new ElementCount(entry.getKey(), entry.getValue().size()))
                    .sorted(
                            Comparator.comparingLong(ElementCount::elementCount).reversed()
                                    .thenComparing(elemCnt -> elemCnt.elementName().toString())
                    )
                    .collect(ImmutableList.toImmutableList());

            return new XmlDocInfo(docUri, allElems.size(), elemCounts);
        }
    }

    private static final Processor saxonProcessor = new Processor(false);

    private static final Logger logger = LoggerFactory.getLogger(XmlInfoPrinter.class);

    public static void main(String[] args) throws SaxonApiException, IOException {
        Objects.requireNonNull(args);
        if (args.length != 1)
            throw new IllegalArgumentException(String.format("Expected precisely 1 argument (the input XML file URI), but got %s ones", args.length));

        var inputFile = URI.create(args[0]);

        var docBuilder = saxonProcessor.newDocumentBuilder();

        logger.info("Parsing document ...");
        var docNode = docBuilder.build(new StreamSource(inputFile.toURL().openStream()));

        logger.atInfo().setMessage("Document parsed (as Saxon node): {}").addArgument(docNode.getBaseURI()).log();
        logger.atInfo()
                .setMessage("Number of (Saxon) elements: {}")
                .addArgument(
                        docNode.axisIterator(Axis.DESCENDANT_OR_SELF)
                                .stream()
                                .filter(n -> n.getNodeKind().equals(XdmNodeKind.ELEMENT))
                                .asListOfNodes()
                                .size()
                )
                .log();

        var xmlDocElem = (ElemNode) new SaxonConverter().convertToXmlNode(docNode);

        logger.info("Ready converting Saxon document to ElemNode");

        var xmlDocInfo = XmlDocInfo.fromDoc(inputFile, xmlDocElem);

        logger.info("Ready creating 'XmlDocInfo' from ElemNode");

        var objectMapper = new ObjectMapper();

        var module = new GuavaModule();
        objectMapper.registerModule(module);

        logger.atInfo()
                .setMessage("JSON output:\n{}")
                .addArgument(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(xmlDocInfo))
                .log();
    }
}
