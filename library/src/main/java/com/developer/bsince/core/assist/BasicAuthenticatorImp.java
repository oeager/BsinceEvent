package com.developer.bsince.core.assist;

import java.io.IOException;
import java.net.Authenticator.RequestorType;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.util.List;

import com.developer.bsince.core.Authenticator;

public final class BasicAuthenticatorImp implements Authenticator {
	
	private BasicAuthenticatorImp(){}

	public static final BasicAuthenticatorImp INSTANCE = new BasicAuthenticatorImp();
	@Override
	public String authenticate(AuthorResponse response) throws IOException {
		List<Challenge> challenges = response.getChallenge();

		URL url = response.getUrl();
		for (int i = 0, size = challenges.size(); i < size; i++) {
			Challenge challenge = challenges.get(i);
			if (!"Basic".equalsIgnoreCase(challenge.getScheme()))
				continue;

			PasswordAuthentication auth = java.net.Authenticator
					.requestPasswordAuthentication(url.getHost(),
							getConnectToInetAddress(response.getProxy(), url),
							url.getPort(), url.getProtocol(),
							challenge.getRealm(), challenge.getScheme(), url,
							RequestorType.SERVER);
			if (auth == null)
				continue;

			return AuthorizationHelper.BASIC(auth.getUserName(), new String(
					auth.getPassword()));
		}
		return null;

	}

	@Override
	public String authenticateProxy(AuthorResponse response) throws IOException {
		List<Challenge> challenges = response.getChallenge();
		URL url = response.getUrl();
		for (int i = 0, size = challenges.size(); i < size; i++) {
			Challenge challenge = challenges.get(i);
			if (!"Basic".equalsIgnoreCase(challenge.getScheme()))
				continue;
			Proxy proxy = response.getProxy();
			InetSocketAddress proxyAddress = (InetSocketAddress) proxy
					.address();
			PasswordAuthentication auth = java.net.Authenticator
					.requestPasswordAuthentication(proxyAddress.getHostName(),
							getConnectToInetAddress(proxy, url),
							proxyAddress.getPort(), url.getProtocol(),
							challenge.getRealm(), challenge.getScheme(), url,
							RequestorType.PROXY);
			if (auth == null)
				continue;

			return AuthorizationHelper.BASIC(auth.getUserName(), new String(
					auth.getPassword()));

		}
		return null;
	}

	private InetAddress getConnectToInetAddress(Proxy proxy, URL url)
			throws IOException {
		return (proxy != null && proxy.type() != Proxy.Type.DIRECT) ? ((InetSocketAddress) proxy
				.address()).getAddress() : InetAddress.getByName(url.getHost());
	}
}
