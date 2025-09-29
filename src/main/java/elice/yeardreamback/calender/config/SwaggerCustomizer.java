package elice.yeardreamback.calender.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Swagger OpenAPI 문서 커스터마이저
 * 공통 응답 예시와 스키마를 추가하여 API 문서의 일관성을 향상시킵니다.
 */
@Component
public class SwaggerCustomizer implements OpenApiCustomizer {

    @Override
    public void customise(OpenAPI openApi) {
        // 공통 응답 컴포넌트 추가
        addCommonResponses(openApi);
        
        // 공통 예시 추가
        addCommonExamples(openApi);
        
        // API 정보 보강
        enhanceApiInfo(openApi);
    }

    /**
     * 공통 응답 컴포넌트 추가
     */
    private void addCommonResponses(OpenAPI openApi) {
        if (openApi.getComponents() == null) {
            return;
        }

        // 400 Bad Request 응답
        ApiResponse badRequestResponse = new ApiResponse()
                .description("잘못된 요청 - 유효성 검사 실패 또는 잘못된 파라미터")
                .content(new Content()
                        .addMediaType("application/json", new MediaType()
                                .example(SwaggerExamples.ErrorResponse.VALIDATION_ERROR)));

        // 401 Unauthorized 응답
        ApiResponse unauthorizedResponse = new ApiResponse()
                .description("인증 필요 - JWT 토큰이 없거나 유효하지 않음")
                .content(new Content()
                        .addMediaType("application/json", new MediaType()
                                .example(SwaggerExamples.ErrorResponse.UNAUTHORIZED_ERROR)));

        // 403 Forbidden 응답
        ApiResponse forbiddenResponse = new ApiResponse()
                .description("접근 권한 없음 - 리소스에 대한 권한이 부족함")
                .content(new Content()
                        .addMediaType("application/json", new MediaType()
                                .example(SwaggerExamples.ErrorResponse.FORBIDDEN_ERROR)));

        // 404 Not Found 응답
        ApiResponse notFoundResponse = new ApiResponse()
                .description("리소스를 찾을 수 없음")
                .content(new Content()
                        .addMediaType("application/json", new MediaType()
                                .example(SwaggerExamples.ErrorResponse.NOT_FOUND_ERROR)));

        // 409 Conflict 응답
        ApiResponse conflictResponse = new ApiResponse()
                .description("데이터 충돌 - 중복된 데이터 또는 비즈니스 규칙 위반")
                .content(new Content()
                        .addMediaType("application/json", new MediaType()
                                .example(SwaggerExamples.ErrorResponse.CONFLICT_ERROR)));

        // 공통 응답을 컴포넌트에 추가
        openApi.getComponents()
                .addResponses("BadRequest", badRequestResponse)
                .addResponses("Unauthorized", unauthorizedResponse)
                .addResponses("Forbidden", forbiddenResponse)
                .addResponses("NotFound", notFoundResponse)
                .addResponses("Conflict", conflictResponse);
    }

    /**
     * 공통 예시 추가
     */
    private void addCommonExamples(OpenAPI openApi) {
        if (openApi.getComponents() == null) {
            return;
        }

        // 이벤트 생성 요청 예시
        Example eventCreateExample = new Example()
                .summary("이벤트 생성 예시")
                .description("새로운 캘린더 이벤트 생성을 위한 요청 데이터 예시")
                .value(SwaggerExamples.CalendarEvent.CREATE_REQUEST);

        // 이벤트 응답 예시
        Example eventResponseExample = new Example()
                .summary("이벤트 응답 예시")
                .description("캘린더 이벤트 API 응답 데이터 예시")
                .value(SwaggerExamples.CalendarEvent.RESPONSE);

        // 카테고리 생성 요청 예시
        Example categoryCreateExample = new Example()
                .summary("카테고리 생성 예시")
                .description("새로운 이벤트 카테고리 생성을 위한 요청 데이터 예시")
                .value(SwaggerExamples.EventCategory.CREATE_REQUEST);

        // 카테고리 응답 예시
        Example categoryResponseExample = new Example()
                .summary("카테고리 응답 예시")
                .description("이벤트 카테고리 API 응답 데이터 예시")
                .value(SwaggerExamples.EventCategory.RESPONSE);

        // 예시를 컴포넌트에 추가
        openApi.getComponents()
                .addExamples("EventCreateRequest", eventCreateExample)
                .addExamples("EventResponse", eventResponseExample)
                .addExamples("CategoryCreateRequest", categoryCreateExample)
                .addExamples("CategoryResponse", categoryResponseExample);
    }

    /**
     * API 정보 보강
     */
    private void enhanceApiInfo(OpenAPI openApi) {
        if (openApi.getInfo() != null) {
            // API 설명에 사용 가이드 추가
            String enhancedDescription = openApi.getInfo().getDescription() + 
                    "\n\n## 사용 가이드\n\n" +
                    "### 인증\n" + SwaggerExamples.ApiGuide.AUTHENTICATION_INFO + "\n\n" +
                    "### 날짜/시간 형식\n" + SwaggerExamples.ApiGuide.DATE_TIME_FORMAT + "\n\n" +
                    "### 페이지네이션\n" + SwaggerExamples.ApiGuide.PAGINATION_INFO;
            
            openApi.getInfo().setDescription(enhancedDescription);
        }

        // 외부 문서 링크 추가 (필요시)
        // ExternalDocumentation externalDocs = new ExternalDocumentation()
        //         .description("캘린더 API 상세 가이드")
        //         .url("https://docs.yeardream.com/calendar-api");
        // openApi.setExternalDocs(externalDocs);
    }
}