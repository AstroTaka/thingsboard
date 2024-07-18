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
package org.thingsboard.server.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.thingsboard.server.common.data.StringUtils;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.OAuth2RegistrationId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.oauth2.OAuth2ClientInfo;
import org.thingsboard.server.common.data.oauth2.OAuth2Registration;
import org.thingsboard.server.common.data.oauth2.OAuth2RegistrationInfo;
import org.thingsboard.server.common.data.oauth2.PlatformType;
import org.thingsboard.server.config.annotations.ApiOperation;
import org.thingsboard.server.dao.oauth2.OAuth2Configuration;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.entitiy.oauth2client.TbOauth2ClientService;
import org.thingsboard.server.service.security.permission.Operation;
import org.thingsboard.server.service.security.permission.Resource;
import org.thingsboard.server.utils.MiscUtils;

import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

import static org.thingsboard.server.controller.ControllerConstants.SYSTEM_AUTHORITY_PARAGRAPH;
import static org.thingsboard.server.controller.ControllerConstants.TENANT_OR_CUSTOMER_AUTHORITY_PARAGRAPH;

@RestController
@TbCoreComponent
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class OAuth2Controller extends BaseController {

    private final OAuth2Configuration oAuth2Configuration;

    private final TbOauth2ClientService tbOauth2ClientService;


    @ApiOperation(value = "Get OAuth2 clients (getOAuth2Clients)", notes = "Get the list of OAuth2 clients " +
            "to log in with, available for such domain scheme (HTTP or HTTPS) (if x-forwarded-proto request header is present - " +
            "the scheme is known from it) and domain name and port (port may be known from x-forwarded-port header)")
    @PostMapping(value = "/noauth/oauth2Clients")
    public List<OAuth2ClientInfo> getOAuth2Clients(HttpServletRequest request,
                                                   @Parameter(description = "Mobile application package name, to find OAuth2 clients " +
                                                           "where there is configured mobile application with such package name")
                                                   @RequestParam(required = false) String pkgName,
                                                   @Parameter(description = "Platform type to search OAuth2 clients for which " +
                                                           "the usage with this platform type is allowed in the settings. " +
                                                           "If platform type is not one of allowable values - it will just be ignored",
                                                           schema = @Schema(allowableValues = {"WEB", "ANDROID", "IOS"}))
                                                   @RequestParam(required = false) String platform) throws ThingsboardException {
        if (log.isDebugEnabled()) {
            log.debug("Executing getOAuth2Clients: [{}][{}][{}]", request.getScheme(), request.getServerName(), request.getServerPort());
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String header = headerNames.nextElement();
                log.debug("Header: {} {}", header, request.getHeader(header));
            }
        }
        PlatformType platformType = null;
        if (StringUtils.isNotEmpty(platform)) {
            try {
                platformType = PlatformType.valueOf(platform);
            } catch (Exception e) {
            }
        }
        if (StringUtils.isNotEmpty(pkgName)) {
            return oAuth2ClientService.getMobileOAuth2Clients(pkgName, platformType);
        } else {
            return oAuth2ClientService.getWebOAuth2Clients(MiscUtils.getDomainNameAndPort(request), platformType);
        }
    }

    @ApiOperation(value = "Save OAuth2 Client Registration (saveOAuth2Client)", notes = SYSTEM_AUTHORITY_PARAGRAPH)
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
    @PostMapping(value = "/oauth2/client")
    public OAuth2Registration saveOAuth2Client(@RequestBody OAuth2Registration oAuth2Registration) throws Exception {
        TenantId tenantId = getTenantId();
        oAuth2Registration.setTenantId(tenantId);
        checkEntity(oAuth2Registration.getId(), oAuth2Registration, Resource.OAUTH2_CLIENT);
        return tbOauth2ClientService.save(oAuth2Registration, getCurrentUser());
    }

    @ApiOperation(value = "Get OAuth2 Client Registration infos (findTenantOAuth2ClientInfos)", notes = SYSTEM_AUTHORITY_PARAGRAPH)
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
    @GetMapping(value = "/oauth2/client/infos")
    public List<OAuth2RegistrationInfo> findTenantOAuth2ClientInfos() throws ThingsboardException {
        return oAuth2ClientService.findOauth2ClientInfosByTenantId(getTenantId());
    }

    @ApiOperation(value = "Get OAuth2 Client Registration by id (getOAuth2ClientById)", notes = SYSTEM_AUTHORITY_PARAGRAPH)
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
    @GetMapping(value = "/oauth2/client/{id}")
    public OAuth2Registration getOAuth2ClientById(@PathVariable UUID id) throws ThingsboardException {
        OAuth2RegistrationId oAuth2RegistrationId = new OAuth2RegistrationId(id);
        return checkEntityId(oAuth2RegistrationId, oAuth2ClientService::findOAuth2ClientById, Operation.READ);
    }

    @ApiOperation(value = "Delete oauth2 client (deleteAsset)",
            notes = "Deletes the asset and all the relations (from and to the asset). Referencing non-existing asset Id will cause an error." + TENANT_OR_CUSTOMER_AUTHORITY_PARAGRAPH)
    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    @RequestMapping(value = "/oauth2/client/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteOauth2Client(@PathVariable UUID id) throws Exception {
        OAuth2RegistrationId oAuth2RegistrationId = new OAuth2RegistrationId(id);
        OAuth2Registration oAuth2Registration = checkOauth2ClientId(oAuth2RegistrationId, Operation.DELETE);
        tbOauth2ClientService.delete(oAuth2Registration, getCurrentUser());
    }

    @ApiOperation(value = "Get OAuth2 log in processing URL (getLoginProcessingUrl)", notes = "Returns the URL enclosed in " +
            "double quotes. After successful authentication with OAuth2 provider, it makes a redirect to this path so that the platform can do " +
            "further log in processing. This URL may be configured as 'security.oauth2.loginProcessingUrl' property in yml configuration file, or " +
            "as 'SECURITY_OAUTH2_LOGIN_PROCESSING_URL' env variable. By default it is '/login/oauth2/code/'" + SYSTEM_AUTHORITY_PARAGRAPH)
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
    @RequestMapping(value = "/oauth2/loginProcessingUrl", method = RequestMethod.GET)
    public String getLoginProcessingUrl() {
        return "\"" + oAuth2Configuration.getLoginProcessingUrl() + "\"";
    }

}
