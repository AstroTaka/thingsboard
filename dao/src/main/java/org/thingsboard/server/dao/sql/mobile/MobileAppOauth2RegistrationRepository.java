/**
 * Copyright © 2016-2024 The Thingsboard Authors
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
package org.thingsboard.server.dao.sql.mobile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.thingsboard.server.common.data.mobile.MobileAppOauth2Registration;
import org.thingsboard.server.dao.model.sql.DomainOauth2RegistrationEntity;
import org.thingsboard.server.dao.model.sql.MobileAppOauth2RegistrationCompositeKey;
import org.thingsboard.server.dao.model.sql.MobileAppOauth2RegistrationEntity;

import java.util.List;
import java.util.UUID;

public interface MobileAppOauth2RegistrationRepository extends JpaRepository<MobileAppOauth2RegistrationEntity, MobileAppOauth2RegistrationCompositeKey> {

    List<MobileAppOauth2RegistrationEntity> findAllByMobileAppId(@Param("mobileAppId") UUID mobileAppId);

}
