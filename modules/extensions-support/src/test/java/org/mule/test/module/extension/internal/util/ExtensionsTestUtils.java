/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.module.extension.internal.util;

import static com.google.common.collect.ImmutableSet.copyOf;
import static java.util.Collections.emptySet;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.commons.lang.ArrayUtils.isEmpty;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.mule.metadata.api.model.MetadataFormat.JAVA;
import org.mule.metadata.api.ClassTypeLoader;
import org.mule.metadata.api.builder.ArrayTypeBuilder;
import org.mule.metadata.api.builder.BaseTypeBuilder;
import org.mule.metadata.api.builder.TypeBuilder;
import org.mule.metadata.api.model.ArrayType;
import org.mule.metadata.api.model.DictionaryType;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.java.api.handler.TypeHandlerManager;
import org.mule.metadata.java.api.utils.ParsingContext;
import org.mule.runtime.api.meta.model.ElementDslModel;
import org.mule.runtime.api.meta.model.EnrichableModel;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.meta.model.ModelProperty;
import org.mule.runtime.api.meta.model.SubTypesModel;
import org.mule.runtime.api.meta.model.config.ConfigurationModel;
import org.mule.runtime.api.meta.model.operation.OperationModel;
import org.mule.runtime.api.meta.model.parameter.ParameterModel;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.extension.api.ExtensionManager;
import org.mule.runtime.extension.api.runtime.config.ConfigurationFactory;
import org.mule.runtime.extension.api.declaration.type.ExtensionsTypeHandlerManagerFactory;
import org.mule.runtime.extension.api.declaration.type.ExtensionsTypeLoaderFactory;
import org.mule.runtime.extension.api.runtime.exception.ExceptionEnricherFactory;
import org.mule.runtime.extension.api.metadata.MetadataResolverFactory;
import org.mule.runtime.extension.api.model.property.ClassLoaderModelProperty;
import org.mule.runtime.extension.api.model.property.ConfigTypeModelProperty;
import org.mule.runtime.extension.api.model.property.ConnectivityModelProperty;
import org.mule.runtime.extension.api.runtime.ConfigurationInstance;
import org.mule.runtime.extension.api.runtime.InterceptorFactory;
import org.mule.runtime.extension.api.runtime.operation.OperationExecutorFactory;
import org.mule.runtime.module.extension.internal.model.property.ConfigurationFactoryModelProperty;
import org.mule.runtime.module.extension.internal.model.property.ExceptionEnricherModelProperty;
import org.mule.runtime.module.extension.internal.model.property.InterceptorsModelProperty;
import org.mule.runtime.module.extension.internal.model.property.MetadataResolverFactoryModelProperty;
import org.mule.runtime.module.extension.internal.model.property.OperationExecutorModelProperty;
import org.mule.runtime.module.extension.internal.runtime.resolver.ValueResolver;

import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public abstract class ExtensionsTestUtils {

  public static final ClassTypeLoader TYPE_LOADER = ExtensionsTypeLoaderFactory.getDefault().createTypeLoader();
  public static final BaseTypeBuilder TYPE_BUILDER = BaseTypeBuilder.create(JAVA);

  public static final String HELLO_WORLD = "Hello World!";

  public static MetadataType toMetadataType(Class<?> type) {
    return TYPE_LOADER.load(type);
  }

  public static ArrayType arrayOf(Class<? extends Collection> clazz, TypeBuilder itemType) {
    ArrayTypeBuilder arrayTypeBuilder = TYPE_BUILDER.arrayType();
    arrayTypeBuilder.id(clazz.getName());
    arrayTypeBuilder.of(itemType);

    return arrayTypeBuilder.build();
  }

  public static DictionaryType dictionaryOf(Class<? extends Map> clazz, TypeBuilder<?> keyTypeBuilder,
                                            TypeBuilder<?> valueTypeBuilder) {
    return TYPE_BUILDER.dictionaryType().id(clazz.getName()).ofKey(keyTypeBuilder).ofValue(valueTypeBuilder).build();
  }

  public static TypeBuilder<?> objectTypeBuilder(Class<?> clazz) {
    BaseTypeBuilder typeBuilder = BaseTypeBuilder.create(JAVA);
    final TypeHandlerManager typeHandlerManager = new ExtensionsTypeHandlerManagerFactory().createTypeHandlerManager();
    typeHandlerManager.handle(clazz, new ParsingContext(), typeBuilder);

    return typeBuilder;
  }

  public static ValueResolver getResolver(Object value) throws Exception {
    return getResolver(value, null, true);
  }

  public static ValueResolver getResolver(Object value, Event event, boolean dynamic, Class<?>... extraInterfaces)
      throws Exception {
    ValueResolver resolver;
    if (isEmpty(extraInterfaces)) {
      resolver = mock(ValueResolver.class);
    } else {
      resolver = mock(ValueResolver.class, withSettings().extraInterfaces(extraInterfaces));
    }

    when(resolver.resolve(event != null ? same(event) : any(Event.class))).thenReturn(value);
    when(resolver.isDynamic()).thenReturn(dynamic);

    return resolver;
  }

  public static ParameterModel getParameter(String name, Class<?> type) {
    ParameterModel parameterModel = mock(ParameterModel.class);
    when(parameterModel.getName()).thenReturn(name);
    when(parameterModel.getType()).thenReturn(toMetadataType(type));
    when(parameterModel.getModelProperty(any())).thenReturn(Optional.empty());
    when(parameterModel.getDslModel()).thenReturn(ElementDslModel.getDefaultInstance());

    return parameterModel;
  }

  public static void stubRegistryKeys(MuleContext muleContext, final String... keys) {
    when(muleContext.getRegistry().get(anyString())).thenAnswer(invocation -> {
      String name = (String) invocation.getArguments()[0];
      if (name != null) {
        for (String key : keys) {
          if (name.contains(key)) {
            return null;
          }
        }
      }

      return RETURNS_DEEP_STUBS.get().answer(invocation);
    });
  }

  public static <C> C getConfigurationFromRegistry(String key, Event muleEvent, MuleContext muleContext) throws Exception {
    return (C) getConfigurationInstanceFromRegistry(key, muleEvent, muleContext).getValue();
  }

  public static ConfigurationInstance getConfigurationInstanceFromRegistry(String key, Event muleEvent,
                                                                           MuleContext muleContext)
      throws Exception {
    ExtensionManager extensionManager = muleContext.getExtensionManager();
    return extensionManager.getConfiguration(key, muleEvent);
  }

  /**
   * Receives to {@link String} representation of two XML files and verify that they are semantically equivalent
   *
   * @param expected the reference content
   * @param actual   the actual content
   * @throws Exception if comparison fails
   */
  public static void compareXML(String expected, String actual) throws Exception {
    XMLUnit.setNormalizeWhitespace(true);
    XMLUnit.setIgnoreWhitespace(true);
    XMLUnit.setIgnoreComments(true);
    XMLUnit.setIgnoreAttributeOrder(false);

    Diff diff = XMLUnit.compareXML(expected, actual);
    if (!(diff.similar() && diff.identical())) {
      System.out.println(actual);
      DetailedDiff detDiff = new DetailedDiff(diff);
      @SuppressWarnings("rawtypes")
      List differences = detDiff.getAllDifferences();
      StringBuilder diffLines = new StringBuilder();
      for (Object object : differences) {
        Difference difference = (Difference) object;
        diffLines.append(difference.toString() + '\n');
      }

      throw new IllegalArgumentException("Actual XML differs from expected: \n" + diffLines.toString());
    }
  }

  public static void mockClassLoaderModelProperty(ExtensionModel extensionModel, ClassLoader classLoader) {
    when(extensionModel.getModelProperty(ClassLoaderModelProperty.class))
        .thenReturn(of(new ClassLoaderModelProperty(classLoader)));
  }

  public static void setRequires(EnrichableModel model, boolean requiresConfig, boolean requiresConnection) {
    if (requiresConfig) {
      when(model.getModelProperty(ConfigTypeModelProperty.class)).thenReturn(of(mock(ConfigTypeModelProperty.class)));
    }

    if (requiresConnection) {
      when(model.getModelProperty(ConnectivityModelProperty.class)).thenReturn(of(mock(ConnectivityModelProperty.class)));
    }
  }

  public static void mockSubTypes(ExtensionModel mockModel, SubTypesModel... subtypes) {
    if (isEmpty(subtypes)) {
      when(mockModel.getSubTypes()).thenReturn(emptySet());
    } else {
      when(mockModel.getSubTypes()).thenReturn(copyOf(subtypes));
    }
  }

  public static void mockExceptionEnricher(EnrichableModel enrichableModel, ExceptionEnricherFactory exceptionEnricherFactory) {
    Optional<ExceptionEnricherModelProperty> property = exceptionEnricherFactory != null
        ? of(new ExceptionEnricherModelProperty(exceptionEnricherFactory))
        : empty();

    when(enrichableModel.getModelProperty(ExceptionEnricherModelProperty.class)).thenReturn(property);
  }

  public static void mockInterceptors(EnrichableModel enrichableModel, List<InterceptorFactory> interceptorFactories) {
    if (interceptorFactories == null) {
      interceptorFactories = ImmutableList.of();
    }

    when(enrichableModel.getModelProperty(InterceptorsModelProperty.class))
        .thenReturn(of(new InterceptorsModelProperty(interceptorFactories)));
  }

  public static void mockConfigurationInstance(ConfigurationModel configurationModel, Object config) {
    ConfigurationFactory configurationFactory = mock(ConfigurationFactory.class);
    when(configurationFactory.newInstance()).thenReturn(config);
    when(configurationFactory.getObjectType()).thenReturn((Class) config.getClass());

    when(configurationModel.getModelProperty(any())).thenAnswer(new Answer<Optional<ModelProperty>>() {

      @Override
      public Optional<ModelProperty> answer(InvocationOnMock invocationOnMock) throws Throwable {
        Class<? extends ModelProperty> propertyType = (Class<? extends ModelProperty>) invocationOnMock.getArguments()[0];
        if (ConfigurationFactoryModelProperty.class.equals(propertyType)) {
          return of(new ConfigurationFactoryModelProperty(configurationFactory));
        }

        return empty();
      }
    });
  }

  public static void mockMetadataResolverFactory(EnrichableModel model, MetadataResolverFactory factory) {
    Optional<MetadataResolverFactoryModelProperty> property = factory != null
        ? of(new MetadataResolverFactoryModelProperty(factory))
        : empty();

    when(model.getModelProperty(MetadataResolverFactoryModelProperty.class)).thenReturn(property);
  }

  public static void mockExecutorFactory(OperationModel operationModel, OperationExecutorFactory operationExecutorFactory) {
    when(operationModel.getModelProperty(OperationExecutorModelProperty.class))
        .thenReturn(of(new OperationExecutorModelProperty(operationExecutorFactory)));
  }
}
