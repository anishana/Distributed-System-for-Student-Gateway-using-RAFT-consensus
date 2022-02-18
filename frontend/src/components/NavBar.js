import * as React from 'react';
import AppBar from '@mui/material/AppBar';
import Box from '@mui/material/Box';
import Toolbar from '@mui/material/Toolbar';
import IconButton from '@mui/material/IconButton';
import Typography from '@mui/material/Typography';
import Menu from '@mui/material/Menu';
import MenuIcon from '@mui/icons-material/Menu';
import Container from '@mui/material/Container';
import Button from '@mui/material/Button';
import MenuItem from '@mui/material/MenuItem';
import { Link } from 'react-router-dom'
// import {Logo} from '../assets/logo'
const pages = ['New Student', 'Students'];
const links = [{
  'header': pages[0],
  'link': '/',
}, {
  'header': pages[1],
  'link': '/list',
}];

export const NavBar = (props) => {
  const [anchorElNav, setAnchorElNav] = React.useState(null);
  // const [anchorElUser, setAnchorElUser] = React.useState(null);

  const handleOpenNavMenu = (event) => {
    setAnchorElNav(event.currentTarget);
  };
  // const handleOpenUserMenu = (event) => {
  //   setAnchorElUser(event.currentTarget);
  // };

  const handleCloseNavMenu = () => {
    setAnchorElNav(null);
  };

  // const handleCloseUserMenu = () => {
  //   setAnchorElUser(null);
  // };


  return (
    <AppBar position="static">
      <Container maxWidth="xl">
        <Toolbar disableGutters>
          <Typography
            variant="h6"
            noWrap
            component="div"
            sx={{ mr: 2, display: { xs: 'none', md: 'flex' } }}
          >
            University at Buffalo
            {/* <img src="../assets/logo.png" alt="University at Buffalo"/> */}
          </Typography>

          <Typography
            variant="h6"
            noWrap
            component="div"
            sx={{ flexGrow: 1, display: { xs: 'flex', md: 'none' } }}
          >
            LOGO
          </Typography>
          <Box sx={{ ml:10, flexGrow: 1, display: { xs: 'none', md: 'flex' } }}>
           
            {links.map((page) => (

              <Link to={page.link} style={{ textDecoration: 'none' }}>
                <Button
                  key={page.header}
                  sx={{ my: 2, color: 'white', display: 'block' }}
                >
                  {page.header}
                </Button>
              </Link>
            ))}
          </Box>


        </Toolbar>
      </Container>
    </AppBar>
  );
};