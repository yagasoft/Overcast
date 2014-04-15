
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

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuth;


/**
 * Does the OAuth web-based authorization flow for apps that can't provide a redirect URI (such
 * as the command-line example apps that come with this SDK). If you're a normal website, use
 * the {@link DbxWebAuth} class instead.
 *
 * <p>
 * Eventually yields an access token that can be used with {@link DbxClient} to make Dropbox API calls. You typically only need to
 * do this for a user when they first use your application. Once you have an access token for that user, it remains valid for
 * years.
 * </p>
 *
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * String userLocale = ...
 * {@link DbxRequestConfig} requestConfig = new DbxRequestConfig("text-edit/0.1", userLocale);
 * {@link DbxAppInfo} appInfo = DbxAppInfo.Reader.readFromFile("api.app");
 * DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(requestConfig, appInfo);
 *
 * String authorizeUrl = webAuth.start();
 * System.out.println("1. Go to " + authorizeUrl);
 * System.out.println("2. Click \"Allow\" (you might have to log in first).");
 * System.out.println("3. Copy the authorization code.");
 * System.out.print("Enter the authorization code here: ");
 *
 * String code = new BufferedReader(new InputStreamReader(System.in)).readLine();
 * if (code == null) return;
 * code = code.trim();
 *
 * {@link DbxAuthFinish} authFinish = webAuth.finish(code);
 *
 * {@link DbxClient} client = new DbxClient(requestConfig, authFinish.accessToken);
 * </pre>
 */
public class DbxWebAuthNoRedirect
{

	private final DbxRequestConfig	requestConfig;
	private final DbxAppInfo		appInfo;
	private final String redirectUri;

	/**
	 * @param appInfo
	 *            Your application's Dropbox API information (the app key and secret).
	 */
	public DbxWebAuthNoRedirect(DbxRequestConfig requestConfig, DbxAppInfo appInfo, String redirectUri)
	{
		if (requestConfig == null)
		{
			throw new IllegalArgumentException("'requestConfig' is null");
		}
		if (appInfo == null)
		{
			throw new IllegalArgumentException("'appInfo' is null");
		}

		this.requestConfig = requestConfig;
		this.appInfo = appInfo;
		this.redirectUri = redirectUri;
	}

	/**
	 * Start authorization. Returns a "authorization URL" on the Dropbox website that gives the
	 * lets the user grant your app access to their Dropbox account.
	 *
	 * <p>
	 * If they choose to grant access, they will be shown an "authorization code", which they need to copy/paste back into your
	 * app, at which point you can call {@link #finish} to get an access token.
	 * </p>
	 */
	public String start()
	{
		return DbxWebAuthHelper.getAuthorizeUrl(appInfo, requestConfig.userLocale, redirectUri, null);
	}

	/**
	 * Call this after the user has visited the authorizaton URL and copy/pasted the authorization
	 * code that Dropbox gave them.
	 *
	 * @param code
	 *            The authorization code shown to the user when they clicked "Allow" on the authorization
	 *            page on the Dropbox website.
	 */
	public DbxAuthFinish finish(String code)
			throws DbxException
	{
		if (code == null)
		{
			throw new IllegalArgumentException("'code' can't be null");
		}

		return DbxWebAuthHelper.finish(appInfo, requestConfig, code, redirectUri);
	}
}
