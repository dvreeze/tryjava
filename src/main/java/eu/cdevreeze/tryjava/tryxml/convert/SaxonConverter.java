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

package eu.cdevreeze.tryjava.tryxml.convert;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import eu.cdevreeze.tryjava.tryxml.simple.Element;
import eu.cdevreeze.tryjava.tryxml.simple.TextNode;
import eu.cdevreeze.tryjava.tryxml.simple.XmlNode;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.Objects;

/**
 * XmlNodeConverter for Saxon XdmNode
 *
 * @author Chris de Vreeze
 */
public class SaxonConverter implements XmlNodeConverter<XdmNode> {
    @Override
    public XmlNode convertToXmlNode(XdmNode node) {
        Objects.requireNonNull(node);

        return switch (node.getNodeKind()) {
            case XdmNodeKind.DOCUMENT -> {
                var elem = node.axisIterator(Axis.CHILD).stream()
                        .filter(n -> n.getNodeKind().equals(XdmNodeKind.ELEMENT))
                        .firstItem();
                // Recursion
                yield convertToXmlNode(elem);
            }
            case XdmNodeKind.ELEMENT -> convertSaxonElemToXmlNode(node);
            case XdmNodeKind.TEXT -> new TextNode(node.getStringValue());
            default ->
                    throw new RuntimeException(String.format("Unsupported node kind: %s", node.getNodeKind().toString()));
        };
    }

    @Override
    public XdmNode convertXmlNode(XmlNode node) {
        Objects.requireNonNull(node);
        throw new UnsupportedOperationException("Operation 'convertXmlNode' not yet supported");
    }

    private Element convertSaxonElemToXmlNode(XdmNode node) {
        assert node.getNodeKind().equals(XdmNodeKind.ELEMENT);

        var name = convertSaxonQNameToQName(node.getNodeName());
        var attributes = node.axisIterator(Axis.ATTRIBUTE).stream()
                .map(n -> Map.entry(convertSaxonQNameToQName(n.getNodeName()), n.getStringValue()))
                .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));

        // Recursion
        var children = node.axisIterator(Axis.CHILD).stream()
                .filter(this::isSupportedNode)
                .map(this::convertToXmlNode)
                .collect(ImmutableList.toImmutableList());

        return new Element(name, attributes, children);
    }

    private QName convertSaxonQNameToQName(net.sf.saxon.s9api.QName saxonQName) {
        var structuredQName = saxonQName.getStructuredQName();

        var nsUri = structuredQName.getURI();
        var localName = structuredQName.getLocalPart();
        var prefix = structuredQName.getPrefix();
        return new QName(nsUri, localName, prefix);
    }

    private QName getAttributeName(XdmNode attr) {
        assert attr.getNodeKind().equals(XdmNodeKind.ATTRIBUTE);

        return convertSaxonQNameToQName(attr.getNodeName());
    }

    private String getAttributeValue(XdmNode attr) {
        assert attr.getNodeKind().equals(XdmNodeKind.ATTRIBUTE);

        return attr.getStringValue();
    }

    private boolean isSupportedNode(XdmNode node) {
        return switch (node.getNodeKind()) {
            case XdmNodeKind.ELEMENT, XdmNodeKind.TEXT -> true;
            default -> false;
        };
    }
}
