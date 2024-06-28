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

package eu.cdevreeze.tryjava.tryxml.console;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import eu.cdevreeze.tryjava.tryxml.convert.SaxonConverter;
import eu.cdevreeze.tryjava.tryxml.parentaware.DocumentElement;
import eu.cdevreeze.tryjava.tryxml.queryapi.ElementQueryApi;
import eu.cdevreeze.tryjava.tryxml.queryapi.ParentAwareElementQueryApi;
import eu.cdevreeze.tryjava.tryxml.simple.Element;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNodeKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.net.URI;
import java.util.Comparator;
import java.util.Map;
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

    public record XmlDocInfo(
            URI docUri,
            int nrOfElems,
            ImmutableList<ElementCount> elemCounts,
            ImmutableMap<ImmutableList<QName>, Long> namePathCounts) {

        public static <E extends ParentAwareElementQueryApi<E>> XmlDocInfo fromDoc(URI docUri, E docElem) {
            ImmutableList<ElementCount> elemCounts =
                    docElem.elementStream().collect(Collectors.groupingBy(
                                    ElementQueryApi::elementName,
                                    Collectors.counting()
                            )).entrySet()
                            .stream()
                            .map(kv -> new ElementCount(kv.getKey(), kv.getValue()))
                            .sorted(
                                    Comparator.comparingLong(ElementCount::elementCount).reversed()
                                            .thenComparing(elemCnt -> elemCnt.elementName().toString())
                            )
                            .collect(ImmutableList.toImmutableList());

            QName docElemName = docElem.elementName();

            ImmutableMap<ImmutableList<QName>, Long> namePathCounts =
                    docElem.elementStream()
                            .collect(Collectors.groupingBy(
                                    e -> getNamePath(e, docElemName),
                                    Collectors.counting()
                            )).entrySet()
                            .stream()
                            .collect(
                                    ImmutableSortedMap.toImmutableSortedMap(
                                            Comparator.comparing(names -> names.stream().map(Object::toString).collect(Collectors.joining(","))),
                                            Map.Entry::getKey,
                                            Map.Entry::getValue));

            return new XmlDocInfo(docUri, (int) docElem.elementStream().count(), elemCounts, namePathCounts);
        }
    }

    private static final Processor saxonProcessor = new Processor(false);

    private static final Logger logger = LoggerFactory.getLogger(XmlInfoPrinter.class);

    public static void main(String[] args) throws SaxonApiException, IOException {
        Objects.requireNonNull(args);
        if (args.length != 1)
            throw new IllegalArgumentException(String.format("Expected precisely 1 argument (the input XML file URI), but got %s ones", args.length));

        var inputFile = URI.create(args[0]);

        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

        var docBuilder = saxonProcessor.newDocumentBuilder();

        logger.info(String.format("Memory usage: %s", memoryBean.getHeapMemoryUsage()));

        logger.info("Parsing document ...");
        var docNode = docBuilder.build(new StreamSource(inputFile.toURL().openStream()));

        logger.info(String.format("Memory usage: %s", memoryBean.getHeapMemoryUsage()));

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

        logger.info(String.format("Memory usage: %s", memoryBean.getHeapMemoryUsage()));

        var xmlDocElem = (Element) new SaxonConverter().convertToXmlNode(docNode);

        logger.info("Ready converting Saxon document to Element");

        logger.info(String.format("Memory usage: %s", memoryBean.getHeapMemoryUsage()));

        DocumentElement documentElement = DocumentElement.create(xmlDocElem);

        logger.info("Ready converting Element to DocumentElement.Element");

        logger.info(String.format("Memory usage: %s", memoryBean.getHeapMemoryUsage()));

        var xmlDocInfo = XmlDocInfo.fromDoc(inputFile, documentElement.documentElement());

        logger.info("Ready creating 'XmlDocInfo' from DocumentElement.Element");

        logger.info(String.format("Memory usage: %s", memoryBean.getHeapMemoryUsage()));

        var objectMapper = new ObjectMapper();

        var module = new GuavaModule();
        objectMapper.registerModule(module);

        logger.info(String.format("Memory usage: %s", memoryBean.getHeapMemoryUsage()));

        logger.atInfo()
                .setMessage("JSON output:\n{}")
                .addArgument(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(xmlDocInfo))
                .log();
    }

    private static <E extends ParentAwareElementQueryApi<E>> ImmutableList<QName> getNamePath(E element, QName docElemName) {
        return element.ancestorOrSelfStream().map(ElementQueryApi::elementName)
                .collect(
                        Collectors.collectingAndThen(
                                ImmutableList.toImmutableList(),
                                p -> ImmutableList.<QName>builder().addAll(p).add(docElemName).build()))
                .reverse();
    }
}
