# jwp-subway-path

## 미션 요구사항
- 1주차 : 지하철 노선에 구간 추가, 삭제 기능, 조회 기능 수정
- 2주차 : 최단 경로 조회 기능
- 선택 : 가장 빠른 경로 조회 기능

## 1주차
### 주차 요구사항
- [ ] 노선에 역 등록 API 신규 구현
- [ ] 노선에 역 제거 API 신규 구현
- [ ] 노선 조회 API 수정
- [ ] 노선 목록 조회 API 수정
- [ ] 통합 테스트
- [ ] 단위 테스트

### 설계
- 역 간 연결
   - 다음 역
      - Station 객체 
   - 거리
      - Distance 객체
      - 어느 역에 연결되느냐에 따라 다름 (Station 객체로 표현 불가)
   - 방향성
      - 이동 가능한 방향만 그래프에 담기
   > 새 객체 생성 (Section) <br/>
   > 필드 : 다음역(Station), 거리(Distance)

  
- 그래프
  > 객체 명 : SubwayGraph <br/>
  > key : Station (역) <br/>
  > value : List Section (이동 가능 구간) <br/>

### 단위 기능 목록

1. 도메인 구현
- [x] Distance 구현
- [x] Section 구현
- [ ] SubwayGraph 구현
- [ ] 그래프에 값 추가
- [ ] 그래프에 값 삭제
- [ ] 그래프 조회
