/*
 * Copyright (c) 2017 pCloud AG
 *
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
 *
 */

package com.pcloud.sdk.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.pcloud.sdk.ApiClient;
import com.pcloud.sdk.ApiError;
import com.pcloud.sdk.Authenticators;
import com.pcloud.sdk.AuthorizationActivity;
import com.pcloud.sdk.AuthorizationData;
import com.pcloud.sdk.AuthorizationRequest;
import com.pcloud.sdk.AuthorizationResult;
import com.pcloud.sdk.DataSource;
import com.pcloud.sdk.PCloudSdk;
import com.pcloud.sdk.RemoteFolder;
import com.pcloud.sdk.internal.IOUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;

public class MainActivity extends Activity {

	private static final int PCLOUD_AUTHORIZATION_REQUEST_CODE = 123;

	private TextView apiKeyView;
	private TextView authorizationResultView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		authorizationResultView = findViewById(R.id.authorizationResult);
		apiKeyView = findViewById(R.id.apiKey);

		findViewById(R.id.authorizeButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				apiKeyView.setText(null);
				authorizationResultView.setText(null);
				//TODO Set YOUR application Client ID
				Intent authIntent = AuthorizationActivity.createIntent(MainActivity.this, AuthorizationRequest.create().setType(AuthorizationRequest.Type.TOKEN).setClientId("XTsnFgtbuxH").setForceAccessApproval(true).addPermission("manageshares").build());
				startActivityForResult(authIntent, PCLOUD_AUTHORIZATION_REQUEST_CODE);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == PCLOUD_AUTHORIZATION_REQUEST_CODE) {
			final AuthorizationData authData = AuthorizationActivity.getResult(data);
			AuthorizationResult result = authData.result;
			authorizationResultView.setText(result.name());

			switch (result) {
				case ACCESS_GRANTED:
					apiKeyView.setText(authData.toString());

					Thread thread = new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								try {
									ApiClient apiClient = PCloudSdk.newClientBuilder().apiHost(authData.apiHost).authenticator(Authenticators.newOAuthAuthenticator(authData.token)).create();

									byte[] ret = "7dkWZDEmrKFWWFwf6nvJX7sHZMqwhHcWydDUgs".getBytes();

									apiClient.createFile(RemoteFolder.ROOT_FOLDER_ID, "LI65pACAWjvkS-xLXXuAZ49MUMSC6BxBnsc=.c9r", DataSource.create(ret)).execute();

								} catch (IOException e) {
									e.printStackTrace();
								} catch (ApiError apiError) {
									apiError.printStackTrace();
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});

					thread.start();

					break;
				case ACCESS_DENIED:
					//TODO: Add proper handling for denied grants.
					Log.d("pCloud", "Account access denied");
					break;
				case AUTH_ERROR:
					//TODO: Add error handling.
					apiKeyView.setText(authData.errorMessage);
					Log.d("pCloud", "Account access grant error:\n" + authData.errorMessage);
					break;
				case CANCELLED:
					//TODO: Handle cancellation.
					Log.d("pCloud", "Account access grant cancelled:");
					break;
			}
		}
	}
}
