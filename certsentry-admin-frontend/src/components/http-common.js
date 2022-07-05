import axios from 'axios'

import store from '../store'

const AXIOS = axios.create({
    baseURL: `/api`,
    timeout: 1000,
});


export default {
    getSecured(uri) {
        return AXIOS.get(uri,{
            auth: {
                username: store.getters.getUserName,
                password: store.getters.getUserPass
            }});
    },
    postSecured(uri,params) {
        return AXIOS.post(uri, params, {
            auth: {
                username: store.getters.getUserName,
                password: store.getters.getUserPass
            }});
    },
    patchSecured(uri,params) {
        return AXIOS.patch(uri, params,{
            auth: {
                username: store.getters.getUserName,
                password: store.getters.getUserPass
            }});
    },
    deleteSecured(uri) {
        return AXIOS.delete(uri,{
            auth: {
                username: store.getters.getUserName,
                password: store.getters.getUserPass
            }});
    },
    get(uri,params) {
        return AXIOS.get(uri,params);
    }
}