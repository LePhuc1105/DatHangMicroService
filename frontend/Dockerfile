FROM nginx:alpine

# Copy the frontend files to the Nginx web root
COPY . /usr/share/nginx/html

# Nginx listens on port 80 by default
EXPOSE 80

# Start Nginx
CMD ["nginx", "-g", "daemon off;"] 