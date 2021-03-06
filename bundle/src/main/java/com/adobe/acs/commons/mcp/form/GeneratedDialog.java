/*
 * #%L
 * ACS AEM Commons Bundle
 * %%
 * Copyright (C) 2017 Adobe
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.adobe.acs.commons.mcp.form;

import com.adobe.acs.commons.mcp.util.AnnotatedFieldDeserializer;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Generates a dialog out of @FormField annotations
 * Ideally your sling model should extend this class to inherit its features
 */
@Model(
        adaptables = {SlingHttpServletRequest.class},
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
@ProviderType
public class GeneratedDialog {
    @Inject
    private Resource resource;

    @Inject
    private SlingHttpServletRequest request;

    @Inject
    private SlingScriptHelper sling;

    private FormComponent form;

    Map<String, FieldComponent> fieldComponents;

    @PostConstruct
    public void init() {
        if (getResource() == null && getRequest() != null) {
            resource = getRequest().getResource();
        }
        getFieldComponents();
    }

    public Map<String, FieldComponent> getFieldComponents() {
        if (fieldComponents == null) {
            fieldComponents = AnnotatedFieldDeserializer.getFormFields(getClass(), getSlingHelper());
        }
        return fieldComponents;
    }

    public Collection<String> getAllClientLibraries() {
        return getClientLibraries(FieldComponent.ClientLibraryType.ALL);
    }

    public Collection<String> getCssClientLibraries() {
        return getClientLibraries(FieldComponent.ClientLibraryType.CSS);
    }

    public Collection<String> getJsClientLibraries() {
        return getClientLibraries(FieldComponent.ClientLibraryType.JS);
    }

    private Collection<String> getClientLibraries(FieldComponent.ClientLibraryType type) {
        LinkedHashSet<String> allLibraries = new LinkedHashSet<>();
        fieldComponents.values().stream()
                .map(c -> c.getClientLibraryCategories().get(type))
                .filter(v -> v != null)
                .forEach(v -> allLibraries.addAll(v));
        return allLibraries;
    }

    /**
     * @return the resource
     */
    public Resource getResource() {
        return resource;
    }

    /**
     * @return the request
     */
    public SlingHttpServletRequest getRequest() {
        return request;
    }

    /**
     * @return the sling helper
     */
    public SlingScriptHelper getSlingHelper() {
        return sling;
    }

    public Resource getFormResource() {
        return getForm().buildComponentResource();
    }

    public FormComponent getForm() {
        if (form == null) {
            form = new FormComponent();
            if (sling != null) {
                form.setHelper(sling);
                form.setPath(sling.getRequest().getResource().getPath());
                form.setAsync(true);
                form.getComponentMetadata().put("granite:id", "mcp-generated-form");
            } else {
                form.setPath("/form");
            }
            getFieldComponents().forEach((name, component) -> form.addComponent(name, component));
        }
        return form;
    }
}
