/*
 * Modern Dynamics
 * Copyright (C) 2021 shartte & Technici4n
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package dev.technici4n.moderndynamics.test;

import dev.technici4n.moderndynamics.test.framework.MdGameTestHelper;
import dev.technici4n.moderndynamics.util.MdId;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

public final class MdGameTests {
    private static final ResourceKey<TestEnvironmentDefinition<?>> TEST_ENVIRONMENT_KEY = ResourceKey.create(Registries.TEST_ENVIRONMENT,
            MdId.of("environment"));

    private static final Identifier TEST_ENVIRONMENT = MdId.of("environment");

    private static final List<Class<?>> testClasses = List.of(
            FluidTransferTest.class,
            ItemDistributionTest.class,
            ItemTransferTest.class);

    private static final Map<Identifier, TestData<ResourceKey<TestEnvironmentDefinition<?>>>> tests = new HashMap<>();

    private MdGameTests() {
    }

    public static void registerFunctions() {
        tests.clear();
        for (var testClass : testClasses) {
            for (var testMethod : testClass.getMethods()) {
                var gametest = testMethod.getDeclaredAnnotation(MdGameTest.class);
                if (gametest != null) {
                    var testData = new TestData<>(
                            TEST_ENVIRONMENT_KEY,
                            MdId.of("empty"),
                            gametest.timeoutTicks(),
                            gametest.setupTicks(),
                            gametest.required(),
                            StructureUtils.getRotationForRotationSteps(gametest.rotationSteps()),
                            gametest.manualOnly(),
                            gametest.attempts(),
                            gametest.requiredSuccesses(),
                            gametest.skyAccess());
                    var testId = MdId.of(testMethod.getName().toLowerCase(Locale.ROOT));
                    Consumer<GameTestHelper> function = helper -> {
                        try {
                            var testObject = testClass.getConstructor().newInstance();
                            testMethod.invoke(testObject, new MdGameTestHelper(helper.testInfo));
                        } catch (ReflectiveOperationException e) {
                            throw new RuntimeException(e);
                        }
                    };
                    ResourceKey<Consumer<GameTestHelper>> functionKey = ResourceKey.create(Registries.TEST_FUNCTION, testId);
                    Registry.register(BuiltInRegistries.TEST_FUNCTION, functionKey, function);
                    tests.put(testId, testData);
                }
            }
        }
    }

    public static void registerTests(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(TEST_ENVIRONMENT);
        for (var entry : tests.entrySet()) {
            var testId = entry.getKey();
            ResourceKey<Consumer<GameTestHelper>> functionKey = ResourceKey.create(Registries.TEST_FUNCTION, testId);
            var realizedTestData = entry.getValue().map(ignored -> environment);
            event.registerTest(entry.getKey(), new FunctionGameTestInstance(functionKey, realizedTestData));
        }
    }

}
