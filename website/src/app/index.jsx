import { Outlet } from 'react-router-dom';
import styles from './index.module.css';
import Header from '../components/header';
import Footer from '../components/footer';
import { CartProvider } from '../context/CartContext';
import { ToastProvider } from '../context/ToastContext';
import ToastContainer from '../components/toast';

const App = () => {
  	return (
		<ToastProvider>
			<CartProvider>
				<div className={styles.app}>
					<Header />
					<div className={styles.container}>
						<Outlet />
					</div>
					<Footer />
					<ToastContainer />
				</div>
			</CartProvider>
		</ToastProvider>
  	)
}

export default App
