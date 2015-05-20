package com.developer.bsince.core.assist;

public final class Challenge {

	private final String scheme;
	private final String realm;

	public Challenge(String scheme, String realm) {
		this.scheme = scheme;
		this.realm = realm;
	}

	/** Returns the authentication scheme */
	public String getScheme() {
		return scheme;
	}

	/** Returns the protection space. */
	public String getRealm() {
		return realm;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Challenge && equal(scheme, ((Challenge) o).scheme)
				&& equal(realm, ((Challenge) o).realm);
	}

	@Override
	public int hashCode() {
		int result = 29;
		result = 31 * result + (realm != null ? realm.hashCode() : 0);
		result = 31 * result + (scheme != null ? scheme.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return scheme + " realm=\"" + realm + "\"";
	}

	private static boolean equal(Object a, Object b) {
		return a == b || (a != null && a.equals(b));
	}
}
