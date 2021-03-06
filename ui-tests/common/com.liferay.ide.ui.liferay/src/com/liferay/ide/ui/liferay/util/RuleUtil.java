/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.ide.ui.liferay.util;

import com.liferay.ide.ui.liferay.support.sdk.SdkSupport;
import com.liferay.ide.ui.liferay.support.server.ServerRunningSupport;
import com.liferay.ide.ui.liferay.support.server.ServerSupport;
import com.liferay.ide.ui.liferay.support.server.Tomcat7xSupport;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;

import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * @author Terry Jia
 */
public class RuleUtil {

	public static RuleChain getRuleChain(TestRule... rules) {
		RuleChain chain = RuleChain.outerRule(rules[0]);

		if (rules.length > 1) {
			for (int i = 1; i < rules.length; i++) {
				chain = chain.around(rules[i]);
			}
		}

		return chain;
	}

	public static RuleChain getTomcat7xRuleChain(SWTWorkbenchBot bot, ServerSupport server) {
		return getRuleChain(server, new Tomcat7xSupport(bot, server));
	}

	public static RuleChain getTomcat7xRunningRuleChain(SWTWorkbenchBot bot, ServerSupport server) {
		return getRuleChain(server, new Tomcat7xSupport(bot, server), new ServerRunningSupport(bot, server));
	}

	public static RuleChain getTomcat7xRunningSdkRuleChain(SWTWorkbenchBot bot, ServerSupport server) {
		return getRuleChain(
			server, new Tomcat7xSupport(bot, server), new SdkSupport(bot, server),
			new ServerRunningSupport(bot, server));
	}

	public static RuleChain getTomcat7xSdkRuleChain(SWTWorkbenchBot bot, ServerSupport server) {
		return getRuleChain(server, new Tomcat7xSupport(bot, server), new SdkSupport(bot, server));
	}

}