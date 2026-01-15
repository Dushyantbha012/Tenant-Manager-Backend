# Build the image
docker build -t tenantmanage .
# Run the container
docker run -p 8080:8080 tenantmanage