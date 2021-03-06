/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.petstore.extension;

import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.core.api.config.ThreadingProfile;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.Sources;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;
import org.mule.runtime.extension.api.annotation.param.Optional;

import java.util.List;

@Extension(name = "petstore", description = "PetStore Test connector")
@Operations(PetStoreOperations.class)
@ConnectionProviders({SimplePetStoreConnectionProvider.class, PooledPetStoreConnectionProvider.class,
    TransactionalPetStoreConnectionProvider.class})
@Sources({PetStoreSource.class})
@Xml(namespaceLocation = "http://www.mulesoft.org/schema/mule/petstore", namespace = "petstore")
public class PetStoreConnector {

  @Parameter
  private List<String> pets;

  @Parameter
  @Optional
  private TlsContextFactory tls;

  @Parameter
  @Optional
  private ThreadingProfile threadingProfile;

  @Parameter
  @Optional
  private PetCage cage;

  @ParameterGroup
  private ExclusiveCashier cashier;

  public List<String> getPets() {
    return pets;
  }

  public TlsContextFactory getTlsContext() {
    return tls;
  }

  public ThreadingProfile getThreadingProfile() {
    return threadingProfile;
  }

  public PetCage getCage() {
    return cage;
  }

  public ExclusiveCashier getCashier() {
    return cashier;
  }
}
