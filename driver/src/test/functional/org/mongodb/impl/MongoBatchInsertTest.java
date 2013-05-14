/*
 * Copyright (c) 2008 - 2013 10gen, Inc. <http://10gen.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mongodb.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mongodb.CommandOperation;
import org.mongodb.DatabaseTestCase;
import org.mongodb.Document;
import org.mongodb.InsertOperation;
import org.mongodb.WriteConcern;
import org.mongodb.codecs.DocumentCodec;
import org.mongodb.command.Count;
import org.mongodb.command.CountCommandResult;
import org.mongodb.operation.MongoFind;
import org.mongodb.operation.MongoInsert;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mongodb.Fixture.getBufferPool;
import static org.mongodb.Fixture.getCredentialList;
import static org.mongodb.Fixture.getOptions;
import static org.mongodb.Fixture.getPrimary;

public class MongoBatchInsertTest extends DatabaseTestCase {
    private MongoSyncConnection connection;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        connection = new DefaultMongoSyncConnectionFactory(getOptions(), getPrimary(), getBufferPool(),
                getCredentialList()).create();
    }

    @After
    public void tearDown() {
        super.tearDown();
        connection.close();
    }


    @Test
    public void testBatchInsert() {
        byte[] hugeByteArray = new byte[1024 * 1024 * 15];

        List<Document> documents = new ArrayList<Document>();
        documents.add(new Document("bytes", hugeByteArray));
        documents.add(new Document("bytes", hugeByteArray));
        documents.add(new Document("bytes", hugeByteArray));
        documents.add(new Document("bytes", hugeByteArray));

        final MongoInsert<Document> insert = new MongoInsert<Document>(documents).writeConcern(WriteConcern.ACKNOWLEDGED);
        new InsertOperation<Document>(collection.getNamespace(), insert, new DocumentCodec(), getBufferPool()).execute(connection);
        assertEquals(documents.size(), new CountCommandResult(new CommandOperation(database.getName(),
                new Count(new MongoFind(), collectionName), new DocumentCodec(), getBufferPool()).execute(connection))
                .getCount());
    }

}