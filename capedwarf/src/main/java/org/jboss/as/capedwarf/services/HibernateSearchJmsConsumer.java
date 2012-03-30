/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.capedwarf.services;

import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.hibernate.search.backend.LuceneWork;
import org.hibernate.search.backend.impl.jms.JMSBackendQueueTask;
import org.hibernate.search.engine.spi.SearchFactoryImplementor;
import org.hibernate.search.indexes.spi.IndexManager;
import org.infinispan.Cache;
import org.infinispan.query.Search;
import org.jboss.logging.Logger;

/**
 * JMS consumer for Hibernate search.
 *
 * @author Matej Lazar
 */
class HibernateSearchJmsConsumer implements MessageListener {

    private static final Logger log = Logger.getLogger(HibernateSearchJmsConsumer.class);
    private static final String PREFIX = "org_jboss_capedwarf_jms_";

    private Cache cache;

    public HibernateSearchJmsConsumer(Cache cache) {
        this.cache = cache;
    }


    @Override
    public void onMessage(Message message) {
        if ( !( message instanceof ObjectMessage ) ) {
            log.warnf("Incorrect message type: %s.", message.getClass() );
            return;
        }
        final ObjectMessage objectMessage = (ObjectMessage) message;
        final String indexName;
        final List<LuceneWork> queue;
        final IndexManager indexManager;

        SearchFactoryImplementor factory = (SearchFactoryImplementor) Search.getSearchManager(getCache()).getSearchFactory();
        try {
            indexName = objectMessage.getStringProperty( JMSBackendQueueTask.INDEX_NAME_JMS_PROPERTY );
            indexManager = factory.getAllIndexesManager().getIndexManager( indexName );
            if ( indexManager == null ) {
                log.warnf("Message received for undefined index: %s.", indexName );
                return;
            }
            queue = indexManager.getSerializer().toLuceneWorks( (byte[]) objectMessage.getObject() );
            indexManager.performOperations( queue, null );
        }
        catch (JMSException e) {
            log.errorf("Unable to retrieve object from message: %s.", message.getClass(), e );
            return;
        }
        catch (ClassCastException e) {
            log.error("Illegal object retrieved from message.", e );
            return;
        }
        //TODO clean up ?
    }


    private Cache getCache() {
        return cache;
    }

}
