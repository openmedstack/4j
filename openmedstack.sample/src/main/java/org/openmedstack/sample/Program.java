package org.openmedstack.sample;

import org.openmedstack.Chassis;
import org.openmedstack.DeploymentConfiguration;
import org.openmedstack.domain.guice.EventStoreModule;

public class Program{
    public static void Main(String... args){
        var configuration = new DeploymentConfiguration();
        configuration.setName("sample");
        var chassis = Chassis.from(configuration).setBuilder(c->new DefaultService(new EventStoreModule()));
        chassis.start();
    }
}
