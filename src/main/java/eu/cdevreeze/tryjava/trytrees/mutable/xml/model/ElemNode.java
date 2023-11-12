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

package eu.cdevreeze.tryjava.trytrees.mutable.xml.model;

import eu.cdevreeze.tryjava.trytrees.mutable.DefaultNode;
import eu.cdevreeze.tryjava.trytrees.mutable.internal.DefaultNodeStreamApi;
import eu.cdevreeze.tryjava.trytrees.mutable.internal.NodeStreamApi;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Element node, offering an element query API
 *
 * @param name
 * @param attributes
 * @param childNodes
 * @author Chris de Vreeze
 */
public record ElemNode(QName name, Map<QName, String> attributes,
                       List<XmlNode> childNodes) implements XmlNode, DefaultNode<ElemNode> {
    public ElemNode {
        Objects.requireNonNull(name);
        Objects.requireNonNull(attributes);
        Objects.requireNonNull(childNodes);
    }

    @Override
    public NodeStreamApi<ElemNode> nodeStreamApi() {
        return singleNodeStreamApi();
    }

    @Override
    public ElemNode self() {
        return this;
    }

    @Override
    public List<ElemNode> findAllChildren() {
        return childNodes.stream().filter(n -> n instanceof ElemNode).map(n -> (ElemNode) n).toList();
    }

    public Optional<String> findAttributeValue(QName attrName) {
        return Optional.ofNullable(attributes.get(attrName));
    }

    public String getAttributeValue(QName attrName) {
        return findAttributeValue(attrName).orElseThrow();
    }

    private static NodeStreamApi<ElemNode> singleNodeStreamApi() {
        return (DefaultNodeStreamApi<ElemNode>) node -> node.findAllChildren().stream();
    }
}
