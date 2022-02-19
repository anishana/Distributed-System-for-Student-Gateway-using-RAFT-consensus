FROM node:alpine

#Working directory of the front end
WORKDIR /frontend

#Copying package.json and files to run
COPY package.json ./
COPY ./ ./

#Install node modules 
RUN npm i

#Run the application
CMD ["npm", "run", "start"]