/*
 * Copyright 2018 Sergej Schaefer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package client;

import admin.IndexAdmin;
import dao.DaoConfig;
import exceptions.ElsaException;
import model.ElsaModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class IndexCreator {
    private IndexCreator() {
    }

    private static final Logger logger = LoggerFactory.getLogger(IndexCreator.class);

    protected static void createIndicesOrEnsureMappingConsistency(final boolean shouldProceed,
                                                                  final Collection<DaoConfig> daoConfigs,
                                                                  final IndexAdmin indexAdmin) throws ElsaException {
        if (!shouldProceed) {
            return;
        }

        logger.info("Applying index updates because createIndicesOrEnsureMappingConsistency was set");
        for (final DaoConfig daoConfig : daoConfigs) {
            final Class<? extends ElsaModel> modelClass = daoConfig.getModelClass();

            if (modelClass.equals(ElsaModel.class)) {
                throw new IllegalArgumentException("Registering interface ElsaModel.class as model is not allowed. " +
                        "Create a model which implements it.");
            }

            if (!indexAdmin.indexExists(daoConfig.getIndexConfig())) {
                indexAdmin.createIndex(daoConfig.getIndexConfig());
                logger.info("Created index: " + daoConfig.getIndexConfig().getIndexName());
            } else {
                indexAdmin.updateMapping(daoConfig.getIndexConfig());
                logger.info("Updated mapping (this is idempotent if you didn't change mapping in the model): " +
                        daoConfig.getIndexConfig().getIndexName());
            }
        }
    }

}
