/**
 *  Copyright 2016-2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.antheminc.oss.nimbus.domain.config.builder;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;

import com.antheminc.oss.nimbus.domain.model.config.AnnotationConfig;

/**
 * @author Soham Chakravarti
 *
 */
public interface AnnotationConfigHandler {
	
	AnnotationConfig handleSingle(AnnotatedElement aElem, Class<? extends Annotation> metaAnnotationType);

	List<AnnotationConfig> handle(AnnotatedElement aElem, Class<? extends Annotation> metaAnnotationType);
	
	List<Annotation> handleRepeatable(AnnotatedElement aElem, Class<? extends Annotation> repeatableMetaAnnotationType);

}
