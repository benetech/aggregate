/*
 * Copyright (C) 2013 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.aggregate.client.odktables;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;

public interface ServerTableServiceAsync {

  void getTables(String officeId, AsyncCallback<ArrayList<TableEntryClient>> callback);

  void getTable(String tableId, AsyncCallback<TableEntryClient> callback);

  void createTable(String tableId, TableDefinitionClient definition,
      AsyncCallback<TableEntryClient> callback);

  void deleteTable(String tableId, AsyncCallback<Void> callback);

}
