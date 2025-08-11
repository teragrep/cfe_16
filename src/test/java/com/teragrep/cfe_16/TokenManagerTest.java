/*
 * HTTP Event Capture to RFC5424 CFE_16
 * Copyright (C) 2021-2025 Suomen Kanuuna Oy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 * Additional permission under GNU Affero General Public License version 3
 * section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with other code, such other code is not for that reason alone subject to any
 * of the requirements of the GNU Affero GPL version 3 as long as this Program
 * is the same Program as licensed from Suomen Kanuuna Oy without any additional
 * modifications.
 *
 * Supplemented terms under GNU Affero General Public License version 3
 * section 7
 *
 * Origin of the software must be attributed to Suomen Kanuuna Oy. Any modified
 * versions must be marked as "Modified version of" The Program.
 *
 * Names of the licensors and authors may not be used for publicity purposes.
 *
 * No rights are granted for use of trade names, trademarks, or service marks
 * which are in The Program if any.
 *
 * Licensee must indemnify licensors and authors for any liability that these
 * contractual assumptions impose on licensors and authors.
 *
 * To the extent this program is licensed as part of the Commercial versions of
 * Teragrep, the applicable Commercial License may apply to this file if you as
 * a licensee so wish it.
 */
package com.teragrep.cfe_16;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Base64;

/*
 * Tests the functionality of TokenManager
 */
public final class TokenManagerTest {

    /*
     * Tests TokenManager's tokenIsMissing() method which checks if
     * HttpServletRequest's header has an authentication token in it.
     */
    @Test
    public void tokenCheckingTest() {
        final TokenManager manager = new TokenManager();

        final String authToken = "AUTH_TOKEN_11111";
        final MockHttpServletRequest requestWithHttpHeaderAuth = new MockHttpServletRequest();
        requestWithHttpHeaderAuth.addHeader("Authorization", authToken);

        final MockHttpServletRequest requestWithBasicAuth = new MockHttpServletRequest();
        requestWithBasicAuth.addHeader("Authorization", "Basic x:" + authToken);

        final MockHttpServletRequest requestWithoutAuth = new MockHttpServletRequest();

        Assertions
                .assertFalse(manager.tokenIsMissing(requestWithHttpHeaderAuth), "Token should be found from the request");
        Assertions.assertFalse(manager.tokenIsMissing(requestWithBasicAuth), "Token should be found from the request");
        Assertions.assertTrue(manager.tokenIsMissing(requestWithoutAuth), "Token should not be found from the request");
    }

    /*
     * Tests TokenManager's isTokenInBasic() method which checks if the
     * HttpServletRequest's header's authentication token is in basic authentication
     * format.
     */
    @Test
    public void basicAuthCheckingTest() {
        final TokenManager manager = new TokenManager();

        final String authToken = "AUTH_TOKEN_11111";
        final String basicAuthHeader = "Basic x:" + authToken;

        Assertions
                .assertTrue(manager.isTokenInBasic(basicAuthHeader), "Authorization header should be in basic format");
        Assertions
                .assertFalse(manager.isTokenInBasic(authToken), "Authorization should not be in basic format when querying with only the authentication token.");
    }

    /*
     * Tests TokenManager's getTokenFromBasic() method which returns the
     * authentication token when it is given in basic authentication format.
     */
    @Test
    public void getTokenFromBasicAuthTest() {
        final TokenManager manager = new TokenManager();

        final String authToken = "AUTH_TOKEN_11111";
        final String basicAuthCredentials = "x:" + authToken;
        final String credentialsEncoded = Base64.getEncoder().encodeToString(basicAuthCredentials.getBytes());
        final String basicAuthHeader = "Basic " + credentialsEncoded;

        Assertions
                .assertEquals(authToken, manager.getTokenFromBasic(basicAuthHeader), "Method should return the authentication token extracted from the Basic Authentication format");
    }

}
