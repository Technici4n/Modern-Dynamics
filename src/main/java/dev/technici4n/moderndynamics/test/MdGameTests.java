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
import java.util.ArrayList;
import java.util.List;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.gametest.framework.TestFunction;
import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder
public class MdGameTests {
    private final List<Class<?>> testClasses = List.of(
            FluidTransferTest.class,
            ItemDistributionTest.class,
            ItemTransferTest.class);

    @GameTestGenerator
    public List<TestFunction> generateTests() {
        var result = new ArrayList<TestFunction>();

        for (var testClass : testClasses) {
            for (var testMethod : testClass.getMethods()) {
                var gametest = testMethod.getDeclaredAnnotation(MdGameTest.class);
                if (gametest != null) {
                    result.add(new TestFunction(
                            gametest.batch(),
                            MdId.MOD_ID + "." + testMethod.getName().toLowerCase(),
                            MdId.of("empty").toString(),
                            StructureUtils.getRotationForRotationSteps(gametest.rotationSteps()),
                            gametest.timeoutTicks(),
                            gametest.setupTicks(),
                            gametest.required(),
                            gametest.requiredSuccesses(),
                            gametest.attempts(),
                            gameTestHelper -> {
                                try {
                                    var testObject = testClass.getConstructor().newInstance();
                                    testMethod.invoke(testObject, new MdGameTestHelper(gameTestHelper.testInfo));
                                } catch (ReflectiveOperationException e) {
                                    throw new RuntimeException(e);
                                }
                            }));
                }
            }
        }

        return result;
    }

}
