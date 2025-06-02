🔐 [Google 로그인 연동 가이드]
✅ 1. 로그인 버튼 클릭 시 아래 주소로 리다이렉트 하게 하면 됩니다(로그인 버튼의 url을 이렇게 만들어주시면 됩니다):

http://localhost:8080/oauth2/authorization/google?redirect_uri=http://localhost:3000/oauth2/callback&mode=login

    redirect_uri는 로그인 성공 후 돌아올 프론트의 주소입니다.

    mode=login은 로그인 흐름임을 백엔드에 전달합니다.

✅ 2. 로그인 성공 시, 백엔드는 JWT 토큰을 생성한 후 아래와 같이 리다이렉트합니다:

http://localhost:3000/oauth2/callback?access_token=...&refresh_token=...

    access_token, refresh_token은 쿼리 파라미터로 전달됩니다.

    프론트는 이 값을 꺼내어 저장해야 합니다.

✅ 3. 프론트에서는 /oauth2/callback 페이지에서 아래 코드로 토큰을 저장하세요:

const params = new URLSearchParams(window.location.search);
const accessToken = params.get("access_token");
const refreshToken = params.get("refresh_token");

localStorage.setItem("accessToken", accessToken);
localStorage.setItem("refreshToken", refreshToken);

✅ 4. 로그인 버튼 예시 (HTML):

<a href="http://localhost:8080/oauth2/authorization/google?redirect_uri=http://localhost:3000/oauth2/callback&mode=login">
  구글 로그인
</a>