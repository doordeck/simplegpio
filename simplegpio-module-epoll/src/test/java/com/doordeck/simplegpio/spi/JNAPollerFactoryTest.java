package com.doordeck.simplegpio.spi;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JNAPollerFactoryTest {

    @Test
    public void testServiceLoading() {
        assertEquals(JNAPollerFactory.class, InputPollerFactoryLocator.locate().getClass());
    }

}
