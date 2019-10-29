package net.bddtrader.acceptancetests.stepdefinitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import net.bddtrader.acceptancetests.actors.Trader;
import net.bddtrader.acceptancetests.endpoints.BDDTraderEndPoints;
import net.bddtrader.acceptancetests.model.MarketPrice;
import net.bddtrader.acceptancetests.model.OrderTrade;
import net.bddtrader.clients.Client;
import net.bddtrader.portfolios.*;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.rest.interactions.Get;
import net.serenitybdd.screenplay.rest.interactions.Post;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.contains;
import static net.bytebuddy.matcher.ElementMatchers.is;
import static net.serenitybdd.screenplay.actors.OnStage.theActorCalled;
import static org.assertj.core.api.Assertions.assertThat;

public class ViewingPositionsStepDefinitons {


    private Client clientForTest;
    private static final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Given("the following market prices:")
    public void marketPrices(List<MarketPrice> marketPrices) {
        marketPrices.forEach(
                this::updateMarketPrice
        );
    }


    @Given("(\\w+) (\\w+) is a registered client")
    public void givenARegisteredClient(String firstName, String lastName) throws IOException {

        clientForTest = new Client(null, firstName, lastName, firstName + "@" + lastName+ ".com");

        theActorCalled("New Client").attemptsTo(
                Post.to(BDDTraderEndPoints.RegisterClient.path())
                        .with(request -> request
                                .header("Content-Type", "application/json")
                                .header("Accept", "application/json")
                                .body(clientForTest))
        );
        assertThat(SerenityRest.lastResponse().statusCode()).isEqualTo(200);
        clientForTest = objectMapper.readValue(SerenityRest.lastResponse().asString(), Client.class);

    }

    @When("Lola has purchased (\\d+) (.*) shares at \\$(.*) price")
    public void whenClientHasPurchasedShares(Integer unitPurchased, String securityCodeShare, double priceByUnitShare) throws JsonProcessingException {


        long portFolioId = getPortFolioIdByClientId(clientForTest.getId());
        OrderTrade order = new OrderTrade(unitPurchased, securityCodeShare, TradeType.Buy, 10000.00);

        Response response = RestAssured.given()
                .pathParam("portfolioId", portFolioId)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(order)
                .post("/api" + BDDTraderEndPoints.PlaceOrder.path());


        assertThat(response.getStatusCode()).isEqualTo(200);
    }


    @Then("her positions should be:")
    public void checkPositionByClientId(final DataTable expectedPositionsTable) throws IOException {


        Positions expectedPositions = convertFromDataTableToPositions(expectedPositionsTable);


        Response response = RestAssured.given()
                .pathParam("clientId", clientForTest.getId())
                .get("/api"+BDDTraderEndPoints.ClientPortfolioPositions.path());

        assertThat(response.getStatusCode()).isEqualTo(200);

        Positions positionsResult = convertJsonToPositions(response.asString());
        assertThat(expectedPositions.getPositions().size()).isEqualTo(positionsResult.getPositions().size());
        for(String item : expectedPositions.getPositions().keySet()){
            assertThat(expectedPositions.getPositions().get(item))
                    .isEqualToComparingOnlyGivenFields(positionsResult.getPositions().get(item),
                            "amount",
                    "totalValueInDollars", "profit");

        }

    }

    private static Positions convertJsonToPositions(String jsonInput) throws IOException {

        List<Position> positionList = objectMapper.readValue(jsonInput, new TypeReference<List<Position>>(){});
        return new Positions(generatePositions(positionList));


    }

    @NotNull
    private Positions convertFromDataTableToPositions(DataTable expectedPositionsTable) {
        List<Position> inputDataTablePosition = expectedPositionsTable.asList(Position.class);

        Map<String, Position> inputPositionMap = generatePositions(inputDataTablePosition);
        return new Positions(inputPositionMap);
    }

    private static Map<String, Position> generatePositions(List<Position> inputDataTablePosition) {

        Map<String, Position> result = new HashMap<>();
        for(Position posItem : inputDataTablePosition){
            result.put(posItem.getSecurityCode(), posItem);
        }
        return result;
    }


    private void updateMarketPrice(MarketPrice marketPrice) {
        theActorCalled("Market Forces").attemptsTo(
                Post.to(BDDTraderEndPoints.UpdatePrice.path())
                        .with(request -> request.pathParam("securityCode", marketPrice.getSecurityCode())
                                .header("Content-Type", "application/json")
                                .body(marketPrice.getPrice()))
        );
        assertThat(SerenityRest.lastResponse().statusCode()).isEqualTo(200);
    }

    private long getPortFolioIdByClientId(final long clientId) {

        ResponseBody responseBody = RestAssured.given()
                .header("Accept" , "application/json")
                .pathParam("clientId", clientId)
                .get("/api" + BDDTraderEndPoints.ClientPortfolio.path())
                .body();

        Portfolio portfolioResult = responseBody.as(Portfolio.class);
        assertThat(portfolioResult.getClientId()).isEqualTo(clientId);
        return portfolioResult.getPortfolioId();
    }



}
