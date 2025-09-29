package elice.yeardreamback.calender.integration;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * 캘린더 API 전체 테스트 스위트
 * 모든 통합 테스트를 포함하여 전체 시스템을 검증합니다.
 */
@Suite
@SuiteDisplayName("캘린더 API 전체 테스트 스위트")
@SelectClasses({
    // 기본 기능 테스트
    CalendarApiIntegrationTest.class,
    
    // 접근 제어 테스트
    AccessControlIntegrationTest.class,
    CalendarAuthenticationIntegrationTest.class,
    CalendarDataIsolationTest.class,
    
    // 데이터 일관성 테스트
    CalendarDataConsistencyTest.class,
    
    // 성능 테스트
    CalendarPerformanceIntegrationTest.class,
    
    // 엣지 케이스 테스트
    CalendarEdgeCaseTest.class,
    
    // 전체 시스템 테스트
    CalendarSystemIntegrationTest.class
})
public class CalendarTestSuite {
    // 테스트 스위트 클래스는 비어있어도 됩니다.
    // @Suite와 @SelectClasses 어노테이션이 모든 것을 처리합니다.
}