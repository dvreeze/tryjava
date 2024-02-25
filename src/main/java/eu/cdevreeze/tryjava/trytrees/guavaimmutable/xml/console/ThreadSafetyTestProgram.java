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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import eu.cdevreeze.tryjava.trytrees.guavaimmutable.xml.convert.SaxonConverter;
import eu.cdevreeze.tryjava.trytrees.guavaimmutable.xml.model.ElemNode;
import eu.cdevreeze.tryjava.trytrees.guavaimmutable.xml.model.XmlNode;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.net.URI;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Thread-safety test program. This program shows that the Guava-based deeply immutable XML trees are thread-safe.
 *
 * @author Chris de Vreeze
 */
public class ThreadSafetyTestProgram {

    private static final Processor saxonProcessor = new Processor(false);

    private static final Logger logger = LoggerFactory.getLogger(ThreadSafetyTestProgram.class);

    private static final QName counterQName = new QName("counter");

    private static final int numberOfDocuments = 100;
    private static final int numberOfIterations = 100;

    public static void main(String[] args) throws Exception {
        URI defaultDocUri = Objects.requireNonNull(ThreadSafetyTestProgram.class.getResource("/books.xml")).toURI();
        var docUri = (args.length == 0) ? defaultDocUri : URI.create(args[0]);

        logger.info(String.format("Parsing %d input XML documents ...", numberOfDocuments));

        ImmutableList<ElemNode> docElems = IntStream.range(0, numberOfDocuments).mapToObj(i -> parseXml(docUri)).collect(ImmutableList.toImmutableList());

        logger.info(String.format("Parsed %d input XML documents", numberOfDocuments));

        var totalNumberOfElems = docElems.stream().mapToInt(e -> e.findAllDescendantsOrSelf().size()).sum();

        logger.info(String.format("Total number of XML elements in %d documents: %d", numberOfDocuments, totalNumberOfElems));

        var sumOfCounters = docElems.stream().mapToInt(ThreadSafetyTestProgram::sumOfCounters).sum();

        logger.info(String.format("Sum of counters (should be 0): %d", sumOfCounters));
        logger.info(String.format("Updating all documents in parallel %d times ...", numberOfIterations));

        // Now functionally updating elements across multiple threads in parallel
        ImmutableList<ElemNode> updatedDocElems = docElems.stream().parallel().map(e -> {
            logger.debug(String.format("Current thread: %s", Thread.currentThread()));
            // Slow, with too fine-grained parallelism
            return Stream.iterate(e, ThreadSafetyTestProgram::incrementCounters)
                    .limit(numberOfIterations)
                    .parallel()
                    .max(Comparator.comparingInt(ThreadSafetyTestProgram::sumOfCounters))
                    .orElseThrow();
        }).collect(ImmutableList.toImmutableList());

        var sumOfCountersAfterwards = updatedDocElems.stream().mapToInt(ThreadSafetyTestProgram::sumOfCounters).sum();

        var expectedSumOfCountersAfterwards = totalNumberOfElems * (numberOfIterations - 1);
        logger.info(String.format("Expected sum of counters afterwards: %d", expectedSumOfCountersAfterwards));
        logger.info(String.format("Actual sum of counters afterwards: %d", sumOfCountersAfterwards));
        logger.info(String.format("Expected and actual counters are equal: %s", sumOfCountersAfterwards == expectedSumOfCountersAfterwards));
    }

    private static ElemNode parseXml(URI docUri) {
        var docBuilder = saxonProcessor.newDocumentBuilder();

        XdmNode docNode;
        try {
            docNode = docBuilder.build(new StreamSource(docUri.toURL().openStream()));
        } catch (SaxonApiException | IOException e) {
            throw new RuntimeException(e);
        }

        return (ElemNode) new SaxonConverter().convertToXmlNode(docNode);
    }

    private static int sumOfCounters(ElemNode elem) {
        return elem.findAllDescendantsOrSelf().stream().mapToInt(
                e -> Integer.parseInt(e.findAttributeValue(counterQName).orElse("0"))
        ).sum();
    }

    private static ElemNode incrementCounters(ElemNode elem) {
        // No side effects within this function. Just "stateless" transformations.
        return transform(elem, ThreadSafetyTestProgram::incrementOwnCounter);
    }

    private static final AtomicLong internalCounter = new AtomicLong(0L);

    private static ElemNode incrementOwnCounter(ElemNode elem) {
        // No side effects within this function. Just "stateless" transformations.
        var previousCounter = Integer.parseInt(elem.findAttributeValue(counterQName).orElse("0"));

        if (internalCounter.getAndIncrement() % 1000L == 0L) {
            logger.debug(String.format("Thread: %s. Previous counter: %d", Thread.currentThread(), previousCounter));
        }

        ImmutableMap<QName, String> startAttrs = ImmutableMap.<QName, String>builder().put(counterQName, "0").putAll(elem.attributes()).buildKeepingLast();

        var updatedAttrs = startAttrs.entrySet().stream().collect(ImmutableMap.toImmutableMap(
                Map.Entry::getKey,
                entry -> String.valueOf(previousCounter + 1)
        ));

        var resultElem = new ElemNode(elem.name(), updatedAttrs, elem.childNodes());
        if (!resultElem.getAttributeValue(counterQName).equals(String.valueOf(previousCounter + 1))) {
            throw new RuntimeException("Corrupt data while updating counter");
        }
        return resultElem;
    }

    private static ElemNode transform(ElemNode elem, Function<ElemNode, ElemNode> f) {
        // Recursive
        ImmutableList<XmlNode> transformedChildren = elem.childNodes().stream().map(ch -> switch (ch) {
            case ElemNode che -> transform(che, f);
            case XmlNode n -> n;
        }).collect(ImmutableList.toImmutableList());

        return f.apply(new ElemNode(elem.name(), elem.attributes(), transformedChildren));
    }
}
