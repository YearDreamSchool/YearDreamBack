echo "코드 빌드 중..."
docker-compose build app

echo "앱 재시작 중..."
docker-compose up -d --no-deps app

echo "배포 완료!"