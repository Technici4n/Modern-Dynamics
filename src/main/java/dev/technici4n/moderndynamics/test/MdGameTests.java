package dev.technici4n.moderndynamics.test;

import dev.technici4n.moderndynamics.test.framework.MdGameTestHelper;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.gametest.framework.TestFunction;
import net.neoforged.neoforge.gametest.GameTestHolder;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@GameTestHolder
public class MdGameTests {
    private final List<Class<?>> testClasses = List.of(
            FluidTransferTest.class,
            ItemDistributionTest.class
    );

    @GameTestGenerator
    public List<TestFunction> generateTests() {
        var result = new ArrayList<TestFunction>();

        for (var testClass : testClasses) {
            for (var testMethod : testClass.getMethods()) {
                var gametest = testMethod.getDeclaredAnnotation(MdGameTest.class);
                if (gametest != null) {
                    result.add(new TestFunction(
                            gametest.batch(),
                            "moderndynamicsc." + testMethod.getName().toLowerCase(),
                            "moderndynamics:empty",
                            StructureUtils.getRotationForRotationSteps(gametest.rotationSteps()),
                            gametest.timeoutTicks(),
                            gametest.setupTicks(),
                            true,
                            gametest.requiredSuccesses(),
                            gametest.attempts(),
                            gameTestHelper -> {
                                try {
                                    var testObject = testClass.getConstructor().newInstance();
                                    testMethod.invoke(testObject, new MdGameTestHelper(gameTestHelper.testInfo));
                                } catch (ReflectiveOperationException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    ));
                }
            }
        }

        return result;
    }

}
