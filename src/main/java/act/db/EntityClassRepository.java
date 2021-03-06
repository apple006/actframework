package act.db;

/*-
 * #%L
 * ACT Framework
 * %%
 * Copyright (C) 2014 - 2017 ActFramework
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

import act.app.App;
import act.app.AppServiceBase;
import act.app.DbServiceManager;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Keep track of SQL entity model classes
 */
public class EntityClassRepository extends AppServiceBase<EntityClassRepository> {

    // map model class set to db service ID
    private ConcurrentMap<String, Set<Class>> modelClasses = new ConcurrentHashMap<>();

    public EntityClassRepository(App app) {
        super(app);
    }

    public void registerModelClass(Class<?> modelClass) {
        DB db = modelClass.getAnnotation(DB.class);
        String dbId = null == db ? DbServiceManager.DEFAULT : db.value();
        registerModelClass(dbId, modelClass);
    }

    private void registerModelClass(String dbId, Class<?> modelClass) {
        Set<Class> set = modelClasses.get(dbId);
        if (null == set) {
            Set<Class> newSet = new HashSet<>();
            set = modelClasses.putIfAbsent(dbId, newSet);
            if (null == set) {
                set = newSet;
            }
        }
        set.add(modelClass);
    }

    public Set<Class> modelClasses(String dbId) {
        return modelClasses.get(dbId);
    }

    @Override
    protected void releaseResources() {
        modelClasses.clear();
    }

    public static void init(App app) {
        new EntityClassRepository(app);
    }

}
