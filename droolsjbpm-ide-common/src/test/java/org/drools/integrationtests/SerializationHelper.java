/*
 * Copyright 2010 JBoss Inc
 *
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
 */

package org.drools.integrationtests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import org.drools.RuleBase;
import org.drools.SessionConfiguration;
import org.drools.StatefulSession;
import org.drools.core.util.DroolsStreamUtils;
import org.drools.impl.EnvironmentFactory;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.kie.marshalling.Marshaller;
import org.kie.marshalling.MarshallerFactory;
import org.kie.marshalling.ObjectMarshallingStrategy;
import org.kie.runtime.KieSession;
import org.kie.runtime.StatefulKnowledgeSession;

/**
 * Marshalling helper class to perform serialize/de-serialize a given object
 */
public class SerializationHelper {
    public static <T> T serializeObject(T obj) throws IOException,
                                              ClassNotFoundException {
        return serializeObject( obj,
                                null );
    }

    public static <T> T serializeObject(T obj,
                                        ClassLoader classLoader) throws IOException,
                                                                ClassNotFoundException {
        return (T) DroolsStreamUtils.streamIn( DroolsStreamUtils.streamOut( obj ),
                                               classLoader );
    }

    public static StatefulSession getSerialisedStatefulSession(StatefulSession session) throws Exception {
        return getSerialisedStatefulSession( session,
                                             true );
    }

    public static StatefulSession getSerialisedStatefulSession(StatefulSession session,
                                                               RuleBase ruleBase) throws Exception {
        return getSerialisedStatefulSession( session,
                                             ruleBase,
                                             true );
    }

    public static StatefulSession getSerialisedStatefulSession(StatefulSession session,
                                                               boolean dispose) throws Exception {
        return getSerialisedStatefulSession( session,
                                             session.getRuleBase(),
                                             dispose );
    }

    public static StatefulSession getSerialisedStatefulSession(StatefulSession session,
                                                               RuleBase ruleBase,
                                                               boolean dispose) throws Exception {
        // Serialize to a byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream( bos );
        out.writeObject( session );
        out.close();
        bos.close();

        // Get the bytes of the serialized object
        final byte[] b1 = bos.toByteArray();

        ByteArrayInputStream bais = new ByteArrayInputStream( b1 );
        StatefulSession session2 = ruleBase.newStatefulSession( bais );
        bais.close();

        bos = new ByteArrayOutputStream();
        out = new ObjectOutputStream( bos );
        out.writeObject( session2 );
        out.close();
        bos.close();

        final byte[] b2 = bos.toByteArray();

        // bytes should be the same.
        if ( !areByteArraysEqual( b1,
                                  b2 ) ) {

            throw new IllegalArgumentException( "byte streams for serialisation test are not equal" );
        }

        session2.setGlobalResolver( session.getGlobalResolver() );

        if ( dispose ) {
            session.dispose();
        }

        return session2;
    }

    public static StatefulKnowledgeSession getSerialisedStatefulKnowledgeSession(StatefulKnowledgeSession ksession,
                                                                                 boolean dispose) throws Exception {

        return getSerialisedStatefulKnowledgeSession( ksession, MarshallerFactory.newSerializeMarshallingStrategy(), dispose );
    }

    public static StatefulKnowledgeSession getSerialisedStatefulKnowledgeSession(StatefulKnowledgeSession ksession,
                                                                                 ObjectMarshallingStrategy strategy,
                                                                                 boolean dispose) throws Exception {

        Marshaller marshaller = MarshallerFactory.newMarshaller( ksession.getKieBase(), new ObjectMarshallingStrategy[]{strategy} );

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        marshaller.marshall( bos,
                             ksession );
        final byte[] b1 = bos.toByteArray();
        bos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream( b1 );
        KieSession ksession2 = marshaller.unmarshall( bais,
                                                      new SessionConfiguration(),
                                                      EnvironmentFactory.newEnvironment() );
        bais.close();

        bos = new ByteArrayOutputStream();
        marshaller.marshall( bos,
                             ksession2 );
        final byte[] b2 = bos.toByteArray();
        bos.close();

        // bytes should be the same.
        if ( !areByteArraysEqual( b1,
                                  b2 ) ) {
            throw new IllegalArgumentException( "byte streams for serialisation test are not equal" );
        }

        ((StatefulKnowledgeSessionImpl) ksession2).session.setGlobalResolver( ((StatefulKnowledgeSessionImpl) ksession).session.getGlobalResolver() );

        if ( dispose ) {
            ksession.dispose();
        }

        return (StatefulKnowledgeSession) ksession2;
    }

    public static boolean areByteArraysEqual(byte[] b1,
                                             byte[] b2) {
        if ( b1.length != b2.length ) {
            return false;
        }

        for ( int i = 0, length = b1.length; i < length; i++ ) {
            if ( b1[i] != b2[i] ) {
                System.out.println( "Difference at " + i + ": [" + b1[i] + "] != [" + b2[i] + "]" );
                return false;
            }
        }

        return true;
    }
}
