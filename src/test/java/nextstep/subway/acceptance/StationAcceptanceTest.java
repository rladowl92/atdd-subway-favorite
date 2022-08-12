package nextstep.subway.acceptance;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.applicaion.dto.StationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static nextstep.subway.acceptance.StationSteps.지하철역_삭제_요청;
import static nextstep.subway.acceptance.StationSteps.지하철역_생성_요청;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철역 관련 기능")
public class StationAcceptanceTest extends AcceptanceTest {
    /**
     * When 지하철역을 생성하면
     * Then 지하철역이 생성된다
     * Then 지하철역 목록 조회 시 생성한 역을 찾을 수 있다
     */
    @DisplayName("지하철역을 생성한다.")
    @Test
    void createStation() {
        // when
        ExtractableResponse<Response> response = 지하철역_생성_요청(adminToken, "강남역");

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());

        // then
        List<String> stationNames =
                RestAssured.given().log().all()
                        .when().get("/stations")
                        .then().log().all()
                        .extract().jsonPath().getList("name", String.class);
        assertThat(stationNames).containsAnyOf("강남역");
    }

    /**
     * When 관리자 권한의 토큰 없이 지하철역을 생성하면
     * Then 401 Unauthorized 응답을 받는다.
     */
    @DisplayName("지하철역 생성 실패 - 권한 없음")
    @Test
    void createStationFail() {
        // when
        ExtractableResponse<Response> response = 지하철역_생성_요청(adminToken, "강남역");

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    /**
     * Given 2개의 지하철역을 생성하고
     * When 지하철역 목록을 조회하면
     * Then 2개의 지하철역을 응답 받는다
     */
    @DisplayName("지하철역을 조회한다.")
    @Test
    void getStations() {
        // given
        지하철역_생성_요청(adminToken, "강남역");
        지하철역_생성_요청(adminToken, "역삼역");

        // when
        ExtractableResponse<Response> stationResponse = RestAssured.given().log().all()
                .when().get("/stations")
                .then().log().all()
                .extract();

        // then
        List<StationResponse> stations = stationResponse.jsonPath().getList(".", StationResponse.class);
        assertThat(stations).hasSize(2);
    }

    /**
     * Given 지하철역을 생성하고
     * When 그 지하철역을 삭제하면
     * Then 그 지하철역 목록 조회 시 생성한 역을 찾을 수 없다
     */
    @DisplayName("지하철역을 제거한다.")
    @Test
    void deleteStation() {
        // given
        ExtractableResponse<Response> createResponse = 지하철역_생성_요청(adminToken, "강남역");

        // when
        String location = createResponse.header("location");
        지하철역_삭제_요청(adminToken, location, "강남역");

        // then
        List<String> stationNames =
                RestAssured.given().log().all()
                        .when().get("/stations")
                        .then().log().all()
                        .extract().jsonPath().getList("name", String.class);
        assertThat(stationNames).doesNotContain("강남역");
    }

    /**
     * Given 지하철역을 생성하고
     * When 관리자 권한의 토큰 없이 지하철역을 삭제하면
     * Then 401 Unauthorized 응답을 받는다.
     */
    @DisplayName("지하철역 제거 실패 - 권한 없음")
    @Test
    void deleteStationFail() {
        // given
        ExtractableResponse<Response> createResponse = 지하철역_생성_요청(adminToken, "강남역");

        // when
        String location = createResponse.header("location");
        ExtractableResponse<Response> response = 지하철역_삭제_요청(adminToken, location, "강남역");

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }
}