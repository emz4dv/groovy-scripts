def call(Map config) {
  writeFile file: 'nginx.conf', text: """
                      events{}
                      http {
                          log_format  main  '\$remote_addr - \$remote_user [\$time_local] "\$request" '
                                            '\$status \$body_bytes_sent \$request_time "\$http_referer" '
                                            '"\$http_user_agent" "\$http_x_forwarded_for"';
                          include /etc/nginx/mime.types;
                          server {
                              listen 80;
                              server_name localhost;
                              access_log  /var/log/nginx/access.log  main;
                              root /usr/share/nginx/html;
                              index index.html;
                              location / {
                                  try_files \$uri \$uri/ /index.html;
                              }
                          }
                      }
  
                  """
  writeFile file: 'Dockerfile', text: """
                    FROM ${config.baseImage}
                    LABEL maintainer="ianashkin@mdi.ru"
                    COPY nginx.conf /etc/nginx/nginx.conf
                    ADD app.tgz /usr/share/nginx/html
                  """

  docker.withRegistry(config.baseImageRegistryUrl, config.baseImageRegistryCredential) {
    image = docker.build("ui-${APP_NAME}:${VERSION}", ".")
  }

  docker.withRegistry(config.imageRegistryUrl, config.imageRegistryCredential) {
    image.push()
  }
}