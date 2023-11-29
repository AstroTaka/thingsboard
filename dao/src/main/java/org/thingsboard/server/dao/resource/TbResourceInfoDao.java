/**
 * Copyright © 2016-2023 The Thingsboard Authors
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
package org.thingsboard.server.dao.resource;

import org.thingsboard.server.common.data.ResourceType;
import org.thingsboard.server.common.data.TbResourceInfo;
import org.thingsboard.server.common.data.TbResourceInfoFilter;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.dao.Dao;

import java.util.List;

public interface TbResourceInfoDao extends Dao<TbResourceInfo> {

    PageData<TbResourceInfo> findAllTenantResourcesByTenantId(TbResourceInfoFilter filter, PageLink pageLink);

    PageData<TbResourceInfo> findTenantResourcesByTenantId(TbResourceInfoFilter filter, PageLink pageLink);

    TbResourceInfo findByTenantIdAndKey(TenantId tenantId, ResourceType resourceType, String resourceKey);

    boolean existsByTenantIdAndResourceTypeAndResourceKey(TenantId tenantId, ResourceType resourceType, String resourceKey);

    List<String> findKeysByTenantIdAndResourceTypeAndResourceKeyStartingWith(TenantId tenantId, ResourceType resourceType, String resourceKeyQuery);

    List<TbResourceInfo> findByTenantIdAndEtagAndKeyStartingWith(TenantId tenantId, String etag, String query);

    TbResourceInfo findByTenantIdAndEtag(TenantId tenantId, ResourceType resourceType, String etag);
}
