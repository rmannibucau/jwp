package com.github.rmannibucau.jwp.deltaspike;

import org.apache.deltaspike.core.impl.config.ConfigurationExtension;
import org.apache.deltaspike.core.spi.activation.ClassDeactivator;
import org.apache.deltaspike.core.spi.activation.Deactivatable;

// just keep configuration extension, we don't use others for now
public class DeltaSpikeExtensionFilter implements ClassDeactivator {
    @Override
    public Boolean isActivated(final Class<? extends Deactivatable> aClass) {
        return ConfigurationExtension.class == aClass;
    }
}
