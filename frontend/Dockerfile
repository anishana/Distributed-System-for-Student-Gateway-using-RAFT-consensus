FROM node:alpine
WORKDIR /frontend
COPY package.json ./
COPY ./ ./
RUN npm i
CMD ["npm", "run", "start"]