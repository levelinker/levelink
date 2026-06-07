README.md 파일 작성 필수 항목:

앱 소개: 

앱 이름:Zero or Hundred, 

요약: 상품의 가격이 올라가는 기존 경매와는 다르게 상품의 가격이 떨어지면서 구매자들이 적절한 가격이라고 생각을 할때 물건을 구매를 하도록 하는 경매이다. 

기획 의도:경매나 이적시장같은 기존에 존재를 하는 마켓에서 각각의 상품은 실제로 데이터베이스의 코스트가 상당하다. 만약 클라이언트에서 떨어지는 상품의 가격을 계산해도 되고, 사실상 수학적으로 특정한 시간대에 그 상품의 가격이 그 가격인 것이 결정이 되기만 하고 이런식으로 숫자가 자연스럽게 떨어진다면 이것은 구매자들에게 긴장감을 주면서 기존 서버에 무리가 가던 이적시장을 새롭게 디자인을 할 수 있다. 

주요 기능 설명 및 스크린샷: 실행 화면 캡처 또는 GIF 애니메이션

기술 스택: 1. UI 및 레이아웃 (Jetpack Compose)

- Compose BOM (Bill of Materials) : 모든 Compose 라이브러리의 버전을 일관되게 유지합니다.
- Compose Material3 : 최신 구글 디자인 가이드라인을 따르는 UI 컴포넌트들을 제공합니다.
- Material Icons Extended : 다양한 아이콘(뒤로가기, 리스트, 홈 등)을 사용하기 위해 추가되었습니다.
2. 아키텍처 및 상태 관리 (Jetpack Libraries)

- Lifecycle Runtime Compose : Compose 생명주기에 맞춰 상태를 안전하게 수집( collectAsStateWithLifecycle )합니다.
- ViewModel Compose : MVVM 패턴의 핵심인 ViewModel을 Compose와 연결합니다.
- Navigation Compose : 화면 간 이동(마켓 ↔ 상세 화면)을 처리하는 내비게이션 엔진입니다.
3. 데이터 저장 및 네트워킹

- Firebase Realtime Database : 실시간 경매 데이터 전송 및 동기화를 담당하는 핵심 백엔드 라이브러리입니다.
- Jetpack DataStore (Preferences) : 사용자의 잔액(Credits)과 고유 UID를 로컬 기기에 안전하게 저장합니다.
4. 이미지 및 유틸리티

- Coil Compose : 상품 이미지를 갤러리나 URL로부터 비동기적으로 로드하고 최적화하여 표시합니다. build.gradle.kts:L77
- Kotlinx Coroutines : 비동기 처리(Firebase 통신, 실시간 가격 계산 루프)를 위해 사용되었습니다.
[기술 스택 요약]

- 언어 : Kotlin
- UI : Jetpack Compose
- DB : Firebase RTDB
- 이미지 : Coil
- 아키텍처 : Clean Architecture + MVVM

자료 및 영상 링크 (필수 포함):

2분 요약 영상 링크 (유튜브 or 다른곳 OK):https://youtube.com/shorts/j_qK3aKWwwU

10분 상세 발표 영상 링크 (유튜브):https://youtu.be/zLPMdIA-o_E

프로젝트 최종 보고서 파일 (PDF 파일 업로드 후 링크 연결):https://drive.google.com/file/d/1JtLjQC6tqq209Q12PhgDs2dOKWvjA9if/view?usp=sharing

APK 파일 다운로드 링크 또는 설치용 QR 코드 (구글 드라이브 업로드 시 외부 공유 권한 허용 필수):https://drive.google.com/file/d/1xX6yq692wWxDhC5VTWxf3qaNoaViE2k3/view?usp=sharing
