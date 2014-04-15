
package com.yagasoft.overcast.implement.dropbox.authorisation;

/*
Copyright (c) 2013 Dropbox Inc., http://www.dropbox.com/

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

import java.util.ArrayList;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxRequestUtil;
import com.dropbox.core.http.HttpRequestor;
import com.dropbox.core.util.StringUtil;


abstract class DbxWebAuthHelper
{

	public static String getAuthorizeUrl(DbxAppInfo appInfo, String userLocale,
			String redirectUri, String state)
	{
		return DbxRequestUtil.buildUrlWithParams(userLocale,
				appInfo.host.web, "1/oauth2/authorize", new String[] {
						"client_id", appInfo.key,
						"response_type", "code",
						"redirect_uri", redirectUri,
						"state", state,
				});
	}

	public static DbxAuthFinish finish(DbxAppInfo appInfo, DbxRequestConfig requestConfig,
			String code, String originalRedirectUri)
			throws DbxException
	{
		if (code == null)
		{
			throw new IllegalArgumentException("'code' can't be null");
		}

		String[] params = {
				"grant_type", "authorization_code",
				"code", code,
				"redirect_uri", originalRedirectUri,
				"locale", requestConfig.userLocale,
		};

		ArrayList<HttpRequestor.Header> headers = new ArrayList<HttpRequestor.Header>();
		String credentials = appInfo.key + ":" + appInfo.secret;
		String base64Credentials = StringUtil.base64Encode(StringUtil.stringToUtf8(credentials));
		headers.add(new HttpRequestor.Header("Authorization", "Basic " + base64Credentials));

		return DbxRequestUtil.doPostNoAuth(requestConfig, appInfo.host.api, "1/oauth2/token",
				params, headers, new DbxRequestUtil.ResponseHandler<DbxAuthFinish>()
				{

					@Override
					public DbxAuthFinish handle(HttpRequestor.Response response) throws DbxException
					{
						if (response.statusCode != 200)
						{
							throw DbxRequestUtil.unexpectedStatus(response);
						}
						return DbxRequestUtil.readJsonFromResponse(DbxAuthFinish.Reader, response.body);
					}
				});
	}
}
