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

package eu.cdevreeze.tryjava.tryxml.queryapi;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Parent aware element query API, returning streams of elements. This is a common API offered by potentially multiple XML
 * element implementations in this library.
 *
 * @author Chris de Vreeze
 */
public interface ParentAwareElementQueryApi<E> extends ElementQueryApi<E> {

    public Stream<E> ancestorOrSelfStream();

    public Stream<E> ancestorStream();

    public Optional<E> parentOption();
}
