import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import javafx.stage.Stage

import java.text.DecimalFormat

class Main extends Application {
    TextField inputRigUpgradeFrequency,
              inputMoneyGenerated,
              inputEnergyCost,
              inputEnergyUsage,
              inputEnergyRigUsage,
              inputCardCost,
              inputRigCost,
              inputStartCount,
              inputHardwareDelay
    List<Map<String, String>> data = []

    public static void main(String[] args) {
        launch(this, args)
    }

    @Override
    void start(Stage primaryStage) throws Exception {
        Label labelCardCost = new Label("Card cost:")
        Label labelRigCost = new Label("Rig cost:")
        Label labelEnergyCost = new Label("Energy cost (per kWh):")
        Label labelEnergyUsage = new Label("Energy usage (in kWh):")
        Label labelEnergyRigUsage = new Label("Energy rig usage (in kWh):")
        Label labelMoneyGenerated = new Label("Money generated:")
        Label labelRigUpgradeFrequency = new Label("Rig upgrade frequency:")
        Label labelStartCount = new Label("Initial Graphic cards:")
        Label labelHardwareDelay = new Label("Delay for Hardware:")
        inputCardCost = new TextField("330")
        inputRigCost = new TextField("764")
        inputEnergyCost = new TextField(".22")
        inputEnergyUsage = new TextField(".140")
        inputEnergyRigUsage = new TextField(".30")
        inputMoneyGenerated = new TextField("3.2")
        inputRigUpgradeFrequency = new TextField("6")
        inputStartCount = new TextField("18")
        inputHardwareDelay = new TextField("8")
        GridPane gridPane = new GridPane()
        gridPane.add(labelCardCost, 0, 0)
        gridPane.add(inputCardCost, 1, 0)
        gridPane.add(labelEnergyUsage, 0, 1)
        gridPane.add(inputEnergyUsage, 1, 1)
        gridPane.add(labelMoneyGenerated, 0, 2)
        gridPane.add(inputMoneyGenerated, 1, 2)
        gridPane.add(labelRigCost, 0, 3)
        gridPane.add(inputRigCost, 1, 3)
        gridPane.add(labelEnergyRigUsage, 0, 4)
        gridPane.add(inputEnergyRigUsage, 1, 4)
        gridPane.add(labelRigUpgradeFrequency, 0, 5)
        gridPane.add(inputRigUpgradeFrequency, 1, 5)
        gridPane.add(labelEnergyCost, 0, 6)
        gridPane.add(inputEnergyCost, 1, 6)
        gridPane.add(labelStartCount, 0, 7)
        gridPane.add(inputStartCount, 1, 7)
        gridPane.add(labelHardwareDelay, 0, 8)
        gridPane.add(inputHardwareDelay, 1, 8)
        gridPane.setPadding(new Insets(10))
        gridPane.setHgap(5)
        gridPane.setVgap(10)

        Button buttonRun = new Button("Run simulation")
        buttonRun.setOnAction() {
            simulate()
        }

        gridPane.add(buttonRun, 0, 9)

        Scene scene = new Scene(gridPane, 600, 600)
        primaryStage.setTitle("Rig Simulator 2000")
        primaryStage.setScene(scene)
        primaryStage.show()
    }

    void simulate() {
        int cardCount = inputStartCount.text as int,
            rigCount = Math.ceil(cardCount / 18) as int,
            delayHardware = inputHardwareDelay.text as int,
            delayedCards = 0,
            delayedRigs = 0
        double currentMoney = 0.0,
               energyCost = inputEnergyCost.text as double,
               energyCostTotal = 0.0,
               energyUsageTotal = 0.0,
               energyCardUsage = (inputEnergyUsage.text as double) * 24,
               energyRigUsage = (inputEnergyRigUsage.text as double) * 24,
               energyUsagePerDay = cardCount * energyCardUsage + rigCount * energyRigUsage,
               upgradeFrequency = inputRigUpgradeFrequency.text as double,
               cardCost = inputCardCost.text as double,
               moneyGenerated = inputMoneyGenerated.text as double,
               rigCost = inputRigCost.text as double
        int[] incomingCards = new int[366 + delayHardware],
              incomingRigs = new int[366 + delayHardware]

        for (int i = 1; i < 366; i++) {
            currentMoney += (cardCount - delayedCards) * moneyGenerated
            delayedRigs -= incomingRigs[i]
            delayedCards -= incomingCards[i]

            Map<String, String> day = [:]
            day.put("day", i as String)
            day.put("cards", cardCount as String)
            day.put("rigs", rigCount as String)
            day.put("money", roundTo2Decimals(currentMoney) as String)
            day.put("energy usage", roundTo2Decimals(energyUsagePerDay) as String)
            day.put("energy cost", roundTo2Decimals(energyUsagePerDay * energyCost) as String)
            day.put("money per day", roundTo2Decimals((cardCount - delayedCards) * moneyGenerated) as String)

            // upgrades
            if (currentMoney / cardCost >= upgradeFrequency && ((rigCount * 18 >= cardCount + upgradeFrequency) || currentMoney > (cardCost * upgradeFrequency) + rigCost)) {
                boolean upgradeable = true
                while (currentMoney >= cardCost && upgradeable) {
                    if (rigCount >= cardCount / 18.0) {
                        incomingCards[i + delayHardware]++
                        delayedCards++
                        cardCount++
                        currentMoney -= cardCost
                    } else if (currentMoney >= rigCost) {
                        incomingRigs[i + delayHardware]++
                        delayedRigs++
                        rigCount++
                        currentMoney -= rigCost
                    } else {
                        upgradeable = false
                    }
                }
            }
            energyUsagePerDay = ((cardCount - delayedCards) * energyCardUsage) + ((rigCount - delayedRigs) * energyRigUsage)
            energyCostTotal += energyUsagePerDay * energyCost
            energyUsageTotal += energyUsagePerDay

            data << day
        }


        for (Map<String, String> day : data) {
            println day
        }

        println "Summary: $cardCount Cards; $rigCount Rigs; ${roundTo2Decimals(energyUsageTotal)} kWh Energy used; ${roundTo2Decimals(energyCostTotal)} Energy cost; ${roundTo2Decimals((cardCount - (inputStartCount.text as int)) * cardCost + (rigCount - Math.ceil((inputStartCount.text as int) / 16.0)) * rigCost)} Money spend;"
    }

    double roundTo2Decimals(double val) {
        DecimalFormat df2 = new DecimalFormat("###.##");
        return Double.valueOf(df2.format(val));
    }
}
